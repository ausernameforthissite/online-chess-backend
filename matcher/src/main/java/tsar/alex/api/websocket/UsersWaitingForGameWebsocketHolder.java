package tsar.alex.api.websocket;

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
import tsar.alex.dto.response.StartGameBadResponse;
import tsar.alex.dto.response.StartGameOkResponse;
import tsar.alex.dto.request.StartGameRequest;
import tsar.alex.dto.response.StartGameResponse;
import tsar.alex.dto.websocket.response.*;
import tsar.alex.dto.websocket.response.FindGameWebsocketResponseEnum.FindGameWebsocketBadResponseEnum;
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
public class UsersWaitingForGameWebsocketHolder {

    private final Map<String, UserWaitingForGame> uwfgMap = new HashMap<>();
    private final ThreadLocalRandom threadLocalRandom;
    private final MatcherMapper matcherMapper;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduledExecutorService;
    private final MatcherService matcherService;

    private final Set<ChessGameTypeWithTimings> blitzGames = Stream.of(BLITZ_3_0, BLITZ_3_2)
            .collect(Collectors.toCollection(HashSet::new));
    private final Set<ChessGameTypeWithTimings> bulletAndRapidGames = Stream.of(BULLET_1_0, BULLET_1_2, RAPID_10_0,
            RAPID_10_5).collect(Collectors.toCollection(HashSet::new));
    private final Set<ChessGameTypeWithTimings> classicGames = Stream.of(CLASSIC_30_0, CLASSIC_30_30)
            .collect(Collectors.toCollection(HashSet::new));

    public void addUserIfPossible(String username, ChessGameTypeWithTimings searchGameType, WebsocketSessionWrapper websocketSessionWrapper) {
        WebsocketCommonUtils.cancelOldTimeoutDisconnectTask(websocketSessionWrapper, true);

        try {
            List<ChessGameUserRatingsRecord> activeGames = matcherService.findActiveGamesRecordsByUsername(
                    username);

            if (activeGames != null && activeGames.size() == 1) {
                throw new WebsocketException(String.format(ALREADY_IN_GAME, activeGames.get(0).getGameId()),
                        WebsocketErrorCodeEnum.CLOSE_CONNECTION_ALREADY_IN_GAME);
            }
        } catch (UnexpectedDatabaseResultException e) {
            throw new WebsocketException(e, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }

        CurrentUserRating userRating = matcherService.getCurrentUserRating(username,
                searchGameType.getGeneralGameType());

        if (userRating == null) {
            throw new WebsocketException(String.format(NO_USER_RATING, username, searchGameType),
                    WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }

        UserWaitingForGame uwfg;
        UserWaitingForGame oldUwfg;

        synchronized (uwfgMap) {
            oldUwfg = uwfgMap.get(username);

            if (oldUwfg != null) {
                WebSocketSession session = oldUwfg.getSessionWrapper().getSession();

                if (session != null && session.isOpen()) {
                    throw new WebsocketException(ALREADY_SEARCHING, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
                }

                uwfg = oldUwfg;
            } else {
                uwfg = new UserWaitingForGame();
                uwfg.setCurrentUserRating(userRating);
                uwfg.setSearchGameType(searchGameType);
            }

            ScheduledFuture<?> timeoutDisconnectTask = scheduledExecutorService.schedule(
                    new TimeoutSearchAbortHandler(username), MatcherConstants.Game_SEARCH_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
            websocketSessionWrapper.setTimeoutDisconnectTask(timeoutDisconnectTask);
            uwfg.setSessionWrapper(websocketSessionWrapper);
            uwfgMap.put(username, uwfg);
        }
    }


    public void cancelSearchIfPossible(String username) {
        UserWaitingForGame uwfg;

        synchronized (uwfgMap) {
            uwfg = uwfgMap.get(username);
            System.out.println("Cancel : " + uwfg);
            if (uwfg != null && uwfg.isWaitingForAnswer()) {
                String responseAsJsonString = mapResponseToJSONString(
                        new FindGameBadResponse(GAME_BEING_CREATED, FindGameWebsocketBadResponseEnum.CANCEL_BAD));
                sendMessageToWebsocketSession(responseAsJsonString, uwfg.getSessionWrapper().getSession());
                return;
            }

            uwfgMap.remove(username);
        }

        if (uwfg != null) {
            WebsocketSessionWrapper websocketSessionWrapper = uwfg.getSessionWrapper();
            String responseAsJsonString = mapResponseToJSONString(new FindGameCancelOkResponse());
            sendMessageToWebsocketSession(responseAsJsonString, websocketSessionWrapper.getSession());
            WebsocketCommonUtils.cancelOldTimeoutDisconnectTask(websocketSessionWrapper, true);
            setDisconnectTimeoutDisconnectTask(websocketSessionWrapper);
        } else {
            System.out.println("No uwfg with username = " + username + " was found");
        }
    }

    private void abortSearchOnTimeoutIfPossible(String username) {
        UserWaitingForGame uwfg;

        synchronized (uwfgMap) {
            uwfg = uwfgMap.get(username);

            if (uwfg != null && uwfg.isWaitingForAnswer()) {
                return;
            }

            uwfgMap.remove(username);
        }

        if (uwfg != null) {
            WebsocketSessionWrapper websocketSessionWrapper = uwfg.getSessionWrapper();
            String responseAsJsonString = mapResponseToJSONString(new FindGameBadResponse(
                    "Can't find enemy for you. It seems nobody wants to play now =( Please, try again later",
                    FindGameWebsocketBadResponseEnum.FIND_GAME_BAD));
            sendMessageToWebsocketSession(responseAsJsonString, websocketSessionWrapper.getSession());
            setDisconnectTimeoutDisconnectTask(websocketSessionWrapper);
        } else {
            System.out.println("No uwfg with username = " + username + " was found");
        }
    }

    public void removeDisconnectedUserIfPossible(String username, String sessionId) {
        UserWaitingForGame uwfg;

        synchronized (uwfgMap) {
            uwfg = uwfgMap.get(username);

            if (uwfg != null) {
                WebsocketSessionWrapper websocketSessionWrapper = uwfg.getSessionWrapper();

                if (uwfg.isWaitingForAnswer()) {
                    return;
                }

                WebSocketSession websocketSession = websocketSessionWrapper.getSession();

                if (websocketSession == null || !sessionId.equals(websocketSession.getId())) {
                    return;
                }
                WebsocketCommonUtils.cancelOldTimeoutDisconnectTask(websocketSessionWrapper, true);
            }
            uwfgMap.remove(username);
        }
    }

    private void setDisconnectTimeoutDisconnectTask(WebsocketSessionWrapper websocketSessionWrapper) {
        WebSocketSession websocketSession = websocketSessionWrapper.getSession();
        if (websocketSession != null) {
            ScheduledFuture<?> timeoutDisconnectTask = scheduledExecutorService.schedule(
                    new TimeoutWebsocketCloseHandler(websocketSession), Constants.SESSION_TO_BE_CLOSED_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
            websocketSessionWrapper.setTimeoutDisconnectTask(timeoutDisconnectTask);
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
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/find_game/response", responseAsJSONString,
                    headers);
        }
    }

    private String mapResponseToJSONString(FindGameWebsocketResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedDelay = 15 * 1000, initialDelay = 15 * 1000)
    public void pickUpPairsAndInitiateGamesBlitz() {
        pickUpPairsAndInitiateGames(blitzGames);
    }

    @Scheduled(fixedDelay = 20 * 1000, initialDelay = 20 * 1000)
    public void pickUpPairsAndInitiateGamesBulletAndRapid() {
        pickUpPairsAndInitiateGames(bulletAndRapidGames);
    }

    @Scheduled(fixedDelay = 30 * 1000, initialDelay = 30 * 1000)
    public void pickUpPairsAndInitiateGamesClassic() {
        pickUpPairsAndInitiateGames(classicGames);
    }


    public void pickUpPairsAndInitiateGames(Set<ChessGameTypeWithTimings> GameTypes) {
        System.out.println("Inside scheduler");

        for (ChessGameTypeWithTimings GameType : GameTypes) {
            List<UserWaitingForGame> uwfgList;
            UserWaitingForGame[] uwfgArray;

            synchronized (uwfgMap) {
                System.out.println(uwfgMap);

                if (uwfgMap.size() < 2) {
                    return;
                }

                uwfgList = uwfgMap.values().stream()
                        .filter(uwfg -> !uwfg.isWaitingForAnswer() && uwfg.getSearchGameType() == GameType)
                        .collect(Collectors.toList());
                int listSize = uwfgList.size();

                if (listSize < 2) {
                    continue;
                }

                if (listSize % 2 == 1) {
                    uwfgList.remove(threadLocalRandom.nextInt(listSize));
                }
                uwfgList.forEach(uwfg -> uwfg.setWaitingForAnswer(true));
            }

            uwfgArray = uwfgList.toArray(UserWaitingForGame[]::new);
            System.out.println(Arrays.toString(uwfgArray));

            Arrays.sort(uwfgArray);

            ExecutorService executorService = Executors.newFixedThreadPool(Math.min(uwfgArray.length / 2, 10));

            for (int i = 0; i < uwfgArray.length; i += 2) {
                executorService.submit(new StartGameHandler(GameType, new Pair<>(uwfgArray[i], uwfgArray[i + 1])));
            }

            executorService.shutdown();
        }
    }

    @AllArgsConstructor
    private class StartGameHandler implements Runnable {

        private ChessGameTypeWithTimings chessGameType;
        private Pair<UserWaitingForGame> uwfgPair;

        @Override
        public void run() {
            try {
                StartGameRequest startGameRequest = matcherMapper.mapToStartGameRequest(chessGameType, uwfgPair);
                System.out.println("Before send");
                StartGameResponse response = new RestTemplate().postForObject("http://localhost:8082/api/game/start",
                        startGameRequest, StartGameResponse.class);
                System.out.println("After response");
                if (response != null) {
                    FindGameWebsocketResponse websocketResponse;

                    if (response instanceof StartGameOkResponse okResponse) {
                        ChessGameUserRatingsRecord chessGameUserRatingsRecord = matcherMapper
                                .mapToChessGameUserRatingsRecord(okResponse, chessGameType, uwfgPair);
                        matcherService.saveChessGameUserRatingsRecord(chessGameUserRatingsRecord);
                        websocketResponse = new FindGameOkResponse(okResponse.getGameId());
                    } else {
                        StartGameBadResponse badResponse = (StartGameBadResponse) response;
                        System.out.println(badResponse.getMessage());
                        websocketResponse = new FindGameBadResponse("Something went wrong!",
                                FindGameWebsocketBadResponseEnum.FIND_GAME_BAD);
                    }

                    String responseAsJsonString = mapResponseToJSONString(websocketResponse);

                    UserWaitingForGame[] uwfgPairArray = uwfgPair.getPairArray();

                    for (UserWaitingForGame uwfg : uwfgPairArray) {
                        WebsocketSessionWrapper websocketSessionWrapper = uwfg.getSessionWrapper();
                        sendMessageToWebsocketSession(responseAsJsonString, websocketSessionWrapper.getSession());
                        WebsocketCommonUtils.cancelOldTimeoutDisconnectTask(websocketSessionWrapper, true);
                        setDisconnectTimeoutDisconnectTask(websocketSessionWrapper);
                    }

                    synchronized (uwfgMap) {
                        for (UserWaitingForGame uwfg : uwfgPairArray) {
                            uwfgMap.remove(uwfg.getCurrentUserRating().getUsername());
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
