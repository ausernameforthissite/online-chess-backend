package tsar.alex.utils.websocket;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketSession;
import tsar.alex.dto.StartMatchBadResponse;
import tsar.alex.dto.StartMatchOkResponse;
import tsar.alex.dto.StartMatchRequest;
import tsar.alex.dto.StartMatchResponse;
import tsar.alex.dto.websocket.response.*;
import tsar.alex.exception.FindMatchCloseConnectionException;
import tsar.alex.exception.FindMatchException;
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

    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduledExecutorService;

    private final MatcherService matcherService;


    public void addUserIfPossible(String username, WebsocketSessionWrapper websocketSessionWrapper) {
        WebsocketCommonUtils.cancelOldTimeoutFinisher(websocketSessionWrapper, true);

        List<ChessMatchUserRatingsRecord> activeMatches = matcherService
                                                            .findActiveMatchesRecordsByUsername(username);
        System.out.println("Active matches: " + activeMatches);
        if (activeMatches != null && activeMatches.size() != 0) {
            if (activeMatches.size() == 1) {
                throw new FindMatchCloseConnectionException("You are already in match with id="
                        + activeMatches.get(0).getMatchId());
            } else {
                throw new FindMatchException("Several active matches were found");
            }
        }

        CurrentUserRating userRating = matcherService.getCurrentUserRating(username);

        if (userRating == null) {
            throw new FindMatchException("No rating record was found for username = " + username);
        }

        UserWaitingForMatch UWFM = new UserWaitingForMatch();
        UWFM.setCurrentUserRating(userRating);
        UserWaitingForMatch oldUWFM;

        synchronized (UWFMmap) {
            oldUWFM = UWFMmap.get(username);

            if (oldUWFM != null) {
                WebSocketSession session = oldUWFM.getSessionWrapper().getSession();

                if (session != null && session.isOpen()) {
                    throw new FindMatchCloseConnectionException("You are already searching for match" +
                            " (maybe from another window or device)");
                }

                UWFM.setWaitingForAnswer(oldUWFM.isWaitingForAnswer());
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
                sendMessageToWebsocketSession(
                        new FindMatchCancelBadResponse("Can't cancel. Match is already being created."),
                        UWFM.getSessionWrapper().getSession());
                return;
            }

            UWFMmap.remove(username);
        }

        if (UWFM != null) {
            WebsocketSessionWrapper websocketSessionWrapper = UWFM.getSessionWrapper();
            sendMessageToWebsocketSession(new FindMatchCancelOkResponse(), websocketSessionWrapper.getSession());
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
            sendMessageToWebsocketSession(new FindMatchBadResponse(
                            "Can't find enemy for you. It seems nobody wants to play now =( Please, try again later"),
                    websocketSessionWrapper.getSession());
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
                WebsocketCommonUtils.cancelOldTimeoutFinisher(websocketSessionWrapper, false);

                if (UWFM.isWaitingForAnswer()) {
                    return;
                }

                WebSocketSession websocketSession = websocketSessionWrapper.getSession();

                if (websocketSession == null || !sessionId.equals(websocketSession.getId())) {
                    return;
                }
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


    private void sendMessageToWebsocketSession(FindMatchWebsocketResponse response, WebSocketSession websocketSession) {
        String sessionId = websocketSession.getId();

        MessageHeaders headers = WebsocketCommonUtils.prepareMessageHeaders(sessionId);

        if (websocketSession.isOpen()) {
            messagingTemplate.convertAndSendToUser(sessionId,"/queue/find_match/response", response, headers);
        }

        System.out.println("Send " + response.getType() + " to " + sessionId);
    }



    @Scheduled(fixedDelay = 30 * 1000, initialDelay = 30 * 1000)
    public void pickUpPairsAndInitiateMatches() {
        System.out.println("Inside scheduler");

        List<UserWaitingForMatch> UWFMList;
        UserWaitingForMatch[] UWFMArray;

        synchronized (UWFMmap) {
            System.out.println(UWFMmap);

            if (UWFMmap.size() < 2) {
                return;
            }

            UWFMList = UWFMmap.values().stream().filter(UWFM -> !UWFM.isWaitingForAnswer())
                                                .collect(Collectors.toList());
            int listSize = UWFMList.size();

            if (listSize < 2) {
                return;
            }

            if (listSize % 2 == 1) {
                UWFMList.remove(threadLocalRandom.nextInt(listSize));
            }
            UWFMList.forEach(UWFM -> UWFM.setWaitingForAnswer(true));
        }

        UWFMArray = UWFMList.toArray(UserWaitingForMatch[]::new);
        System.out.println(Arrays.toString(UWFMArray));

        Arrays.sort(UWFMArray);

        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(UWFMArray.length / 2, 5));

        for (int i = 0; i < UWFMArray.length; i += 2) {
            UWFMArray[i].setWaitingForAnswer(true);
            UWFMArray[i + 1].setWaitingForAnswer(true);
            executorService.submit(new StartMatchHandler(new Pair<>(UWFMArray[i], UWFMArray[i+1])));
        }

        executorService.shutdown();
    }

    @AllArgsConstructor
    private class StartMatchHandler implements Runnable {

        private Pair<UserWaitingForMatch> UWFMPair;

        @Override
        public void run() {
            try {
                StartMatchRequest startMatchRequest = matcherMapper.mapToStartMatchRequest(UWFMPair);
                StartMatchResponse response = new RestTemplate().postForObject(
                        "http://localhost:8082/api/match/start", startMatchRequest, StartMatchResponse.class);

                if (response != null) {
                    FindMatchWebsocketResponse websocketResponse;

                    if (response instanceof StartMatchOkResponse okResponse) {
                        ChessMatchUserRatingsRecord chessMatchUserRatingsRecord = matcherMapper
                                .mapToChessMatchUserRatingsRecord(okResponse, UWFMPair);
                        matcherService.saveChessMatchUserRatingsRecord(chessMatchUserRatingsRecord);
                        websocketResponse = new FindMatchOkResponse(okResponse.getMatchId());
                    } else {
                        StartMatchBadResponse badResponse = (StartMatchBadResponse) response;
                        System.out.println(badResponse.getMessage());
                        websocketResponse = new FindMatchBadResponse("Something went wrong!");
                    }

                    UserWaitingForMatch[] UWFMPairArray = UWFMPair.getPairArray();

                    for (UserWaitingForMatch UWFM : UWFMPairArray) {
                        WebsocketSessionWrapper websocketSessionWrapper = UWFM.getSessionWrapper();
                        sendMessageToWebsocketSession(websocketResponse, websocketSessionWrapper.getSession());
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
