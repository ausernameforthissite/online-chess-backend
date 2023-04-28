package tsar.alex.utils.websocket;

import static tsar.alex.model.ChessGameTypeWithTimings.*;
import static tsar.alex.utils.CommonTextConstants.*;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketSession;
import tsar.alex.dto.response.StartMatchBadResponse;
import tsar.alex.dto.response.StartMatchOkResponse;
import tsar.alex.dto.request.StartMatchRequest;
import tsar.alex.dto.response.StartMatchResponse;
import tsar.alex.dto.websocket.response.*;
import tsar.alex.dto.websocket.response.FindMatchWebsocketResponseEnum.FindMatchWebsocketBadResponseEnum;
import tsar.alex.exception.UnexpectedDatabaseResultException;
import tsar.alex.exception.WebsocketErrorCodeEnum;
import tsar.alex.exception.WebsocketException;
import tsar.alex.mapper.MatcherMapper;
import tsar.alex.model.*;
import tsar.alex.service.MatcherService;
import tsar.alex.utils.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UsersWaitingForMatchWebsocketHolder {

    private final Map<String, UserWaitingForMatch> UWFMmap = new HashMap<>();

    private final ThreadLocalRandom threadLocalRandom;
    private final MatcherMapper matcherMapper;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduledExecutorService;
    private final MatcherService matcherService;

    private final Set<ChessGameTypeWithTimings> blitzGames = Stream.of(BLITZ_3_0, BLITZ_3_2)
            .collect(Collectors.toCollection(HashSet::new));
    private final Set<ChessGameTypeWithTimings> bulletAndRapidGames = Stream.of(BULLET_1_0, BULLET_1_2, RAPID_10_0, RAPID_10_5)
            .collect(Collectors.toCollection(HashSet::new));
    private final Set<ChessGameTypeWithTimings> classicGames = Stream.of(CLASSIC_30_0, CLASSIC_30_30)
            .collect(Collectors.toCollection(HashSet::new));

    public void addUserIfPossible(String username, ChessGameTypeWithTimings searchGameType, WebsocketSessionWrapper websocketSessionWrapper) {
        WebsocketCommonUtils.cancelOldTimeoutFinisher(websocketSessionWrapper, true);

        try {
            List<ChessMatchUserRatingsRecord> activeMatches = matcherService.findActiveMatchesRecordsByUsername(username);

            if (activeMatches != null && activeMatches.size() == 1) {
                throw new WebsocketException(String.format(ALREADY_IN_MATCH, activeMatches.get(0).getMatchId()), WebsocketErrorCodeEnum.CLOSE_CONNECTION_ALREADY_IN_MATCH);
            }
        } catch (UnexpectedDatabaseResultException e) {
            throw new WebsocketException(e, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }


        CurrentUserRating userRating = matcherService.getCurrentUserRating(username, searchGameType.getGeneralGameType());

        if (userRating == null) {
            throw new WebsocketException(String.format(NO_USER_RATING, username, searchGameType), WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }

        UserWaitingForMatch UWFM = new UserWaitingForMatch();

        UserWaitingForMatch oldUWFM;

        synchronized (UWFMmap) {
            oldUWFM = UWFMmap.get(username);

            if (oldUWFM != null) {
                WebSocketSession session = oldUWFM.getSessionWrapper().getSession();

                if (session != null && session.isOpen()) {
                    throw new WebsocketException(ALREADY_SEARCHING, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
                }

                UWFM = oldUWFM;
            } else {
                UWFM.setCurrentUserRating(userRating);
                UWFM.setSearchGameType(searchGameType);
            }

            ScheduledFuture<?> timeoutFinisher = scheduledExecutorService.schedule(
                    new TimeoutSearchAbortHandler(username),
                    MatcherConstants.MATCH_SEARCH_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
            websocketSessionWrapper.setTimeoutFinisher(timeoutFinisher);
            UWFM.setSessionWrapper(websocketSessionWrapper);
            UWFMmap.put(username, UWFM);
        }
    }


    public void cancelSearchIfPossible(String username) {
        UserWaitingForMatch UWFM;

        synchronized (UWFMmap) {
            UWFM = UWFMmap.get(username);
            System.out.println("Cancel : " + UWFM);
            if (UWFM != null && UWFM.isWaitingForAnswer()) {
                String responseAsJsonString = mapResponseToJSONString(new FindMatchBadResponse("Can't cancel. Match is already being created.", FindMatchWebsocketBadResponseEnum.CANCEL_BAD));
                sendMessageToWebsocketSession(responseAsJsonString, UWFM.getSessionWrapper().getSession());
                return;
            }

            UWFMmap.remove(username);
        }

        if (UWFM != null) {
            WebsocketSessionWrapper websocketSessionWrapper = UWFM.getSessionWrapper();
            String responseAsJsonString = mapResponseToJSONString(new FindMatchCancelOkResponse());
            sendMessageToWebsocketSession(responseAsJsonString, websocketSessionWrapper.getSession());
            WebsocketCommonUtils.cancelOldTimeoutFinisher(websocketSessionWrapper, true);
            setDisconnectTimeoutFinisher(websocketSessionWrapper);
        } else {
            System.out.println("No UWFM with username = " + username + " was found");
        }
    }

    private void abortSearchOnTimeoutIfPossible(String username) {
        UserWaitingForMatch UWFM;

        synchronized (UWFMmap) {
            UWFM = UWFMmap.get(username);

            if (UWFM != null && UWFM.isWaitingForAnswer()) {
                return;
            }

            UWFMmap.remove(username);
        }

        if (UWFM != null) {
            WebsocketSessionWrapper websocketSessionWrapper = UWFM.getSessionWrapper();
            String responseAsJsonString = mapResponseToJSONString(new FindMatchBadResponse(
                    "Can't find enemy for you. It seems nobody wants to play now =( Please, try again later", FindMatchWebsocketBadResponseEnum.FIND_MATCH_BAD));
            sendMessageToWebsocketSession(responseAsJsonString, websocketSessionWrapper.getSession());
            setDisconnectTimeoutFinisher(websocketSessionWrapper);
        } else {
            System.out.println("No UWFM with username = " + username + " was found");
        }
    }

    public void removeDisconnectedUserIfPossible(String username, String sessionId) {
        UserWaitingForMatch UWFM;

        synchronized (UWFMmap) {
            UWFM = UWFMmap.get(username);

            if (UWFM != null) {
                WebsocketSessionWrapper websocketSessionWrapper = UWFM.getSessionWrapper();

                if (UWFM.isWaitingForAnswer()) {
                    return;
                }

                WebSocketSession websocketSession = websocketSessionWrapper.getSession();

                if (websocketSession == null || !sessionId.equals(websocketSession.getId())) {
                    return;
                }
                WebsocketCommonUtils.cancelOldTimeoutFinisher(websocketSessionWrapper, true);
            }
            UWFMmap.remove(username);
        }
    }


    private void setDisconnectTimeoutFinisher(WebsocketSessionWrapper websocketSessionWrapper) {
        WebSocketSession websocketSession = websocketSessionWrapper.getSession();
        if (websocketSession != null) {
            ScheduledFuture<?> timeoutFinisher = scheduledExecutorService.schedule(
                    new TimeoutWebsocketCloseHandler(websocketSession),
                    Constants.SESSION_TO_BE_CLOSED_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
            websocketSessionWrapper.setTimeoutFinisher(timeoutFinisher);
        }
    }


    private void sendMessageToWebsocketSession(String responseAsJSONString, WebSocketSession websocketSession) {
        String sessionId = websocketSession.getId();
        String shortMessage;

        if (responseAsJSONString.length() < 10) {
            shortMessage = responseAsJSONString;
        } else if (responseAsJSONString.length() < 20) {
            shortMessage = responseAsJSONString.substring(9);
        } else {
            shortMessage = responseAsJSONString.substring(9, 20);
        }
        System.out.println("Sending websocket response " + shortMessage + " to session id " + sessionId);

        MessageHeaders headers = WebsocketCommonUtils.prepareMessageHeaders(sessionId);

        if (websocketSession.isOpen()) {
            messagingTemplate.convertAndSendToUser(sessionId,"/queue/find_match/response", responseAsJSONString, headers);
        }
    }

    private String mapResponseToJSONString(FindMatchWebsocketResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @Scheduled(fixedDelay = 15 * 1000, initialDelay = 15 * 1000)
    public void pickUpPairsAndInitiateMatchesBlitz() {
        pickUpPairsAndInitiateMatches(blitzGames);
    }

    @Scheduled(fixedDelay = 20 * 1000, initialDelay = 20 * 1000)
    public void pickUpPairsAndInitiateMatchesBulletAndRapid() {
        pickUpPairsAndInitiateMatches(bulletAndRapidGames);
    }

    @Scheduled(fixedDelay = 30 * 1000, initialDelay = 30 * 1000)
    public void pickUpPairsAndInitiateMatchesClassic() {
        pickUpPairsAndInitiateMatches(classicGames);
    }



    public void pickUpPairsAndInitiateMatches(Set<ChessGameTypeWithTimings> gameTypes) {
        System.out.println("Inside scheduler");

        for (ChessGameTypeWithTimings gameType : gameTypes) {
            List<UserWaitingForMatch> UWFMList;
            UserWaitingForMatch[] UWFMArray;

            synchronized (UWFMmap) {
                System.out.println(UWFMmap);

                if (UWFMmap.size() < 2) {
                    return;
                }

                UWFMList = UWFMmap.values().stream().filter(UWFM -> !UWFM.isWaitingForAnswer() && UWFM.getSearchGameType() == gameType)
                        .collect(Collectors.toList());
                int listSize = UWFMList.size();

                if (listSize < 2) {
                    continue;
                }

                if (listSize % 2 == 1) {
                    UWFMList.remove(threadLocalRandom.nextInt(listSize));
                }
                UWFMList.forEach(UWFM -> UWFM.setWaitingForAnswer(true));
            }

            UWFMArray = UWFMList.toArray(UserWaitingForMatch[]::new);
            System.out.println(Arrays.toString(UWFMArray));

            Arrays.sort(UWFMArray);

            ExecutorService executorService = Executors.newFixedThreadPool(Math.min(UWFMArray.length / 2, 10));

            for (int i = 0; i < UWFMArray.length; i += 2) {
                UWFMArray[i].setWaitingForAnswer(true);
                UWFMArray[i + 1].setWaitingForAnswer(true);
                executorService.submit(new StartMatchHandler(gameType, new Pair<>(UWFMArray[i], UWFMArray[i+1])));
            }

            executorService.shutdown();
        }
    }

    @AllArgsConstructor
    private class StartMatchHandler implements Runnable {

        private ChessGameTypeWithTimings chessGameType;
        private Pair<UserWaitingForMatch> UWFMPair;

        @Override
        public void run() {
            try {
                StartMatchRequest startMatchRequest = matcherMapper.mapToStartMatchRequest(chessGameType, UWFMPair);
                System.out.println("Before send");
                StartMatchResponse response = new RestTemplate().postForObject(
                        "http://localhost:8082/api/match/start", startMatchRequest, StartMatchResponse.class);
                System.out.println("After response");
                if (response != null) {
                    FindMatchWebsocketResponse websocketResponse;

                    if (response instanceof StartMatchOkResponse okResponse) {
                        ChessMatchUserRatingsRecord chessMatchUserRatingsRecord = matcherMapper
                                .mapToChessMatchUserRatingsRecord(okResponse, chessGameType, UWFMPair);
                        matcherService.saveChessMatchUserRatingsRecord(chessMatchUserRatingsRecord);
                        websocketResponse = new FindMatchOkResponse(okResponse.getMatchId());
                    } else {
                        StartMatchBadResponse badResponse = (StartMatchBadResponse) response;
                        System.out.println(badResponse.getMessage());
                        websocketResponse = new FindMatchBadResponse("Something went wrong!", FindMatchWebsocketBadResponseEnum.FIND_MATCH_BAD);
                    }

                    String responseAsJsonString = mapResponseToJSONString(websocketResponse);

                    UserWaitingForMatch[] UWFMPairArray = UWFMPair.getPairArray();

                    for (UserWaitingForMatch UWFM : UWFMPairArray) {
                        WebsocketSessionWrapper websocketSessionWrapper = UWFM.getSessionWrapper();
                        sendMessageToWebsocketSession(responseAsJsonString, websocketSessionWrapper.getSession());
                        WebsocketCommonUtils.cancelOldTimeoutFinisher(websocketSessionWrapper, true);
                        setDisconnectTimeoutFinisher(websocketSessionWrapper);
                    }

                    synchronized (UWFMmap) {
                        for (UserWaitingForMatch UWFM : UWFMPairArray) {
                            UWFMmap.remove(UWFM.getCurrentUserRating().getUsername());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @AllArgsConstructor
    private class TimeoutSearchAbortHandler implements Runnable {

        private final String username;

        @Override
        public void run() {
            abortSearchOnTimeoutIfPossible(username);
        }
    }
}
