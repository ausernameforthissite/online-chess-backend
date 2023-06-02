package tsar.alex.api.websocket;

import static tsar.alex.utils.CommonTextConstants.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.WebSocketSession;
import tsar.alex.dto.websocket.response.*;
import tsar.alex.dto.websocket.response.ChessGameWebsocketResponseEnum.ChessGameWebsocketBadResponseEnum;
import tsar.alex.exception.WebsocketErrorCodeEnum;
import tsar.alex.exception.WebsocketException;
import tsar.alex.model.*;
import tsar.alex.service.GameWebsocketService;
import tsar.alex.utils.*;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public class ChessGameWebsocketRoom {

    public final ReentrantLock reentrantLock = new ReentrantLock();

    private final Map<String, WebsocketSessionWrapper> usersSubscribedToGame = new ConcurrentHashMap<>(2);
    private final ScheduledFuture<?>[] timeoutDisconnectTasks = new ScheduledFuture[4];
    private final String gameId;
    private final ChessGameTypeWithTimings gameType;
    private final UsersInGameWithOnlineStatusesAndTimings usersInGameWithOnlineStatusesAndTimings;
    private boolean finished;
    private int lastMoveNumber = -1;
    private int drawOfferMoveNumber = -1;
    private ChessColor drawOfferUserColor;

    private final ChessGameWebsocketRoomsHolder chessGameWebsocketRoomsHolder;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduledExecutorService;
    private final GameWebsocketService gameWebsocketService;


    public void setLastMoveNumber(int lastMoveNumber) {
        this.lastMoveNumber = lastMoveNumber;
    }

    public void addSubscribedUserIfPossible(String username, WebsocketSessionWrapper websocketSessionWrapper) {
        WebsocketSessionWrapper oldWebsocketSessionWrapper = usersSubscribedToGame.get(username);

        if (oldWebsocketSessionWrapper != null) {
            throw new WebsocketException(ALREADY_SUBSCRIBED, WebsocketErrorCodeEnum.CLOSE_CONNECTION_ALREADY_SUBSCRIBED);
        }
        usersSubscribedToGame.put(username, websocketSessionWrapper);

        ChessColor userColor = usersInGameWithOnlineStatusesAndTimings.getColorByUsername(username);

        if (userColor == null) {
            return;
        }

        ScheduledFuture<?> oldTimeoutDisconnectTask = getTimeoutDisconnectTask(userColor, TimeoutTypeEnum.DISCONNECTED);
        if (oldTimeoutDisconnectTask != null) {
            oldTimeoutDisconnectTask.cancel(true);
        }
        usersInGameWithOnlineStatusesAndTimings.setOnlineStatusByUserColor(userColor, true);

        ChessGameUserSubscribedResponse userSubscribedResponse = new ChessGameUserSubscribedResponse(userColor);
        String responseAsJSONString = mapResponseToJSONString(userSubscribedResponse);

        for (Map.Entry<String, WebsocketSessionWrapper> userSessionWrapper : usersSubscribedToGame.entrySet()) {
            if (!userSessionWrapper.getKey().equals(username)) {
                sendMessageToWebsocketSession(responseAsJSONString, userSessionWrapper.getValue().getSession());
            }
        }
    }

    public void sendInfo(String username) {
        WebsocketSessionWrapper websocketSessionWrapper = usersSubscribedToGame.get(username);
        ChessGameInfoResponse response = new ChessGameInfoResponse(lastMoveNumber,
                                                                    getCurrentUsersOnlineStatusesAndTimings(),
                                                                    gameType.getInitialTimeMS(),
                                                                    ChessGameConstants.FIRST_MOVE_TIME_LEFT_MS,
                                                                    ChessGameConstants.LEFT_GAME_TIMEOUT_MS);

        sendMessageToWebsocketSession(mapResponseToJSONString(response), websocketSessionWrapper.getSession());
    }

    private CurrentUsersOnlineStatusesAndTimings getCurrentUsersOnlineStatusesAndTimings() {
        boolean whiteUserOnline = usersInGameWithOnlineStatusesAndTimings.isWhiteUserOnline();
        boolean blackUserOnline = usersInGameWithOnlineStatusesAndTimings.isBlackUserOnline();

        long whiteTimeLeftMS;
        long blackTimeLeftMS;

        long whiteReconnectTimeLeftMS;
        long blackReconnectTimeLeftMS;

        if (whiteUserOnline) {
            whiteReconnectTimeLeftMS = -1;
        } else {
            whiteReconnectTimeLeftMS = getTimeLeftByColorAndType(ChessColor.WHITE, TimeoutTypeEnum.DISCONNECTED);
        }

        if (blackUserOnline) {
            blackReconnectTimeLeftMS = -1;
        } else {
            blackReconnectTimeLeftMS = getTimeLeftByColorAndType(ChessColor.BLACK, TimeoutTypeEnum.DISCONNECTED);
        }

        if (lastMoveNumber < 1) {
            long initialTime = gameType.getInitialTimeMS();
            whiteTimeLeftMS = initialTime;
            blackTimeLeftMS = initialTime;
            long userFirstMoveTimeLeftMS;


            if (lastMoveNumber == -1) {
                userFirstMoveTimeLeftMS = getTimeLeftByColorAndType(ChessColor.WHITE, TimeoutTypeEnum.TIME_IS_UP);
            } else {
                userFirstMoveTimeLeftMS = getTimeLeftByColorAndType(ChessColor.BLACK, TimeoutTypeEnum.TIME_IS_UP);
            }

            return new CurrentUsersOnlineStatusesAndTimingsFirstMove(whiteUserOnline, whiteTimeLeftMS,
                    whiteReconnectTimeLeftMS, blackUserOnline, blackTimeLeftMS, blackReconnectTimeLeftMS,
                    userFirstMoveTimeLeftMS);
        }

        whiteTimeLeftMS = getTimeLeftByColorAndType(ChessColor.WHITE, TimeoutTypeEnum.TIME_IS_UP);
        blackTimeLeftMS = getTimeLeftByColorAndType(ChessColor.BLACK, TimeoutTypeEnum.TIME_IS_UP);

        return new CurrentUsersOnlineStatusesAndTimings(whiteUserOnline, whiteTimeLeftMS, whiteReconnectTimeLeftMS,
                                                blackUserOnline, blackTimeLeftMS, blackReconnectTimeLeftMS);
    }

    public void removeDisconnectedUser(String username, String sessionId) {

        ChessColor userColor = usersInGameWithOnlineStatusesAndTimings.getColorByUsername(username);

        if (userColor == null) {
            usersSubscribedToGame.remove(username);
            return;
        }

        WebsocketSessionWrapper websocketSessionWrapper = usersSubscribedToGame.get(username);
        if (websocketSessionWrapper == null || !websocketSessionWrapper.getSession().getId().equals(sessionId)) {
            return;
        }

        usersSubscribedToGame.remove(username);

        usersInGameWithOnlineStatusesAndTimings.setOnlineStatusByUserColor(userColor, false);

        if (lastMoveNumber >= 1) {
            setTimeoutDisconnectTask(userColor, TimeoutTypeEnum.DISCONNECTED, ChessGameConstants.LEFT_GAME_TIMEOUT_MS);
        }
        ChessGameUserDisconnectedResponse userDisconnectedResponse = new ChessGameUserDisconnectedResponse(userColor);
        String responseAsJSONString = mapResponseToJSONString(userDisconnectedResponse);

        for (Map.Entry<String, WebsocketSessionWrapper> userSessionWrapper : usersSubscribedToGame.entrySet()) {
            sendMessageToWebsocketSession(responseAsJSONString, userSessionWrapper.getValue().getSession());
        }
    }

    public void makeMoveOkResponse(ChessMove chessMove) {
        ChessColor newUserColor = ChessGameUtils.getUserColorByMoveNumber(lastMoveNumber++);
        ChessColor oldUserColor = ChessColor.getInvertedColor(newUserColor);
        Future<?> oldTimeoutDisconnectTask = getTimeoutDisconnectTask(oldUserColor, TimeoutTypeEnum.TIME_IS_UP);
        if (oldTimeoutDisconnectTask != null) {
            oldTimeoutDisconnectTask.cancel(true);
        }

        if (lastMoveNumber >= 1) {
            usersInGameWithOnlineStatusesAndTimings.setTimeLefts(chessMove);
        }

        if (lastMoveNumber == 1 && !usersInGameWithOnlineStatusesAndTimings.isWhiteUserOnline()) {
            setTimeoutDisconnectTask(ChessColor.WHITE, TimeoutTypeEnum.DISCONNECTED, ChessGameConstants.LEFT_GAME_TIMEOUT_MS);
        }

        long timeLeft = lastMoveNumber < 1 ? ChessGameConstants.FIRST_MOVE_TIME_LEFT_MS :
                                             chessMove.getTimeLeftByUserColor(newUserColor);
        setTimeoutDisconnectTask(newUserColor, TimeoutTypeEnum.TIME_IS_UP, timeLeft);

        ChessGameMoveOkResponse chessGameMoveOkResponse = new ChessGameMoveOkResponse(chessMove);
        String responseAsJSONString = mapResponseToJSONString(chessGameMoveOkResponse);

        for (Map.Entry<String, WebsocketSessionWrapper> userSessionWrapper : usersSubscribedToGame.entrySet()) {
            sendMessageToWebsocketSession(responseAsJSONString, userSessionWrapper.getValue().getSession());
        }
    }

    public void sendBadResponse(@NotNull ChessGameWebsocketBadResponseEnum badResponseType, String username, String errorMessage) {
        WebsocketSessionWrapper websocketSessionWrapper = usersSubscribedToGame.get(username); // Can it be null? If it can, it can be NPE two lines below
        String responseAsJSONString = mapResponseToJSONString(new ChessGameBadResponse(errorMessage, badResponseType));
        sendMessageToWebsocketSession(responseAsJSONString, websocketSessionWrapper.getSession());
    }

    public void makeDrawOffer(String username) {
        ChessColor userColor = usersInGameWithOnlineStatusesAndTimings.getColorByUsername(username);
        if (userColor == null) {
            sendBadResponse(ChessGameWebsocketBadResponseEnum.DRAW_BAD, username,
                        "Only players can offer draw!");
            return;
        }

        if (lastMoveNumber < 1) {
            sendBadResponse(ChessGameWebsocketBadResponseEnum.DRAW_BAD, username,
                    "You can't offer draw before the second turn!");
            return;
        }

        if (lastMoveNumber < drawOfferMoveNumber) {
            sendBadResponse(ChessGameWebsocketBadResponseEnum.DRAW_BAD, username,
                    "Draw can't be offered twice on the same move.");
            return;
        }

        if (lastMoveNumber == drawOfferMoveNumber && userColor == drawOfferUserColor) {
            sendBadResponse(ChessGameWebsocketBadResponseEnum.DRAW_BAD, username,
                    "You need to wait one more turn before make another draw offer.");
            return;
        }

        drawOfferMoveNumber = lastMoveNumber + 1;
        drawOfferUserColor = userColor;

        ChessGameDrawResponse chessGameDrawResponse = new ChessGameDrawResponse(userColor);
        String responseAsJSONString = mapResponseToJSONString(chessGameDrawResponse);

        for (Map.Entry<String, WebsocketSessionWrapper> userSessionWrapper : usersSubscribedToGame.entrySet()) {
            if (!userSessionWrapper.getKey().equals(username)) {
                sendMessageToWebsocketSession(responseAsJSONString, userSessionWrapper.getValue().getSession());
            }
        }
    }

    public void rejectDrawOffer(String username) {
        checkDrawActionPossibility(username, ChessGameWebsocketBadResponseEnum.REJECT_DRAW_BAD);

        drawOfferMoveNumber = -1;
        ChessGameRejectDrawResponse rejectDrawResponse = new ChessGameRejectDrawResponse();
        String responseAsJSONString = mapResponseToJSONString(rejectDrawResponse);

        for (Map.Entry<String, WebsocketSessionWrapper> userSessionWrapper : usersSubscribedToGame.entrySet()) {
            if (!userSessionWrapper.getKey().equals(username)) {
                sendMessageToWebsocketSession(responseAsJSONString, userSessionWrapper.getValue().getSession());
            }
        }
    }

    public void acceptDrawOffer(String username) {
        checkDrawActionPossibility(username, ChessGameWebsocketBadResponseEnum.ACCEPT_DRAW_BAD);

        ChessGameResult gameResult = new ChessGameResult();
        gameResult.setDraw(true);
        gameResult.setMessage("Игроки согласились на ничью.");
        finishGameWebsockets(gameWebsocketService.finishGame(gameId, gameResult));
    }

    private void checkDrawActionPossibility(String username, ChessGameWebsocketBadResponseEnum drawBadResponseType) {
        if (drawOfferUserColor == null || lastMoveNumber > drawOfferMoveNumber) {
            sendBadResponse(drawBadResponseType, username, "Draw offer doesn't exist or is expired!");
            return;
        }

        ChessColor userColor = usersInGameWithOnlineStatusesAndTimings.getColorByUsername(username);
        ChessColor userToWhomDrawWasOffered = ChessColor.getInvertedColor(drawOfferUserColor);

        if (userColor != userToWhomDrawWasOffered) {
            sendBadResponse(drawBadResponseType, username, "Only " +
                    userToWhomDrawWasOffered.name().toLowerCase() + " color user can accept/reject draw offer!");
        }
    }

    public void surrender(String username) {
        ChessColor userColor = usersInGameWithOnlineStatusesAndTimings.getColorByUsername(username);

        if (userColor == null) {
            sendBadResponse(ChessGameWebsocketBadResponseEnum.SURRENDER_BAD, username,
                    "Only players can surrender!");
        }

        ChessGameResult gameResult = new ChessGameResult();
        gameResult.setWinnerColor(ChessColor.getInvertedColor(userColor));
        gameResult.setMessage((userColor == ChessColor.WHITE ? "White" : "Black") + " player has surrendered");
        finishGameWebsockets(gameWebsocketService.finishGame(gameId, gameResult));
    }

    public void finishGameWebsockets(ChessGameResult gameResult) {
        finishGameWebsockets(gameResult, null);
    }

    public void finishGameWebsockets(ChessGameResult gameResult, ChessColor userColor, TimeoutTypeEnum timeoutType) {
        finishGameWebsockets(gameResult, getTimeoutDisconnectTask(userColor, timeoutType));
    }

    private void finishGameWebsockets(ChessGameResult gameResult, Future<?> currentTimeoutDisconnectTask) {
        chessGameWebsocketRoomsHolder.removeGameWebsocketRoom(gameId);

        for (Future<?> timeoutDisconnectTask : timeoutDisconnectTasks) {
            if (timeoutDisconnectTask != null && timeoutDisconnectTask != currentTimeoutDisconnectTask) {
                timeoutDisconnectTask.cancel(true);
            }
        }

        ChessGameResultResponse gameResultResponse = new ChessGameResultResponse(gameResult);
        String responseAsJSONString = mapResponseToJSONString(gameResultResponse);

        for (Map.Entry<String, WebsocketSessionWrapper> userSessionWrapper : usersSubscribedToGame.entrySet()) {
            WebsocketSessionWrapper websocketSessionWrapper = userSessionWrapper.getValue();
            WebSocketSession websocketSession = websocketSessionWrapper.getSession();
            sendMessageToWebsocketSession(responseAsJSONString, websocketSession);

            ScheduledFuture<?> timeoutDisconnectTask = scheduledExecutorService.schedule(
                                                    new TimeoutWebsocketCloseHandler(websocketSession),
                                                    Constants.SESSION_TO_BE_CLOSED_TIMEOUT_MS,
                                                    TimeUnit.MILLISECONDS);
            websocketSessionWrapper.setTimeoutDisconnectTask(timeoutDisconnectTask);
        }

        finished = true;
    }

    private String mapResponseToJSONString(ChessGameWebsocketResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessageToWebsocketSession(String responseAsJSONString, WebSocketSession websocketSession) {
        String sessionId = websocketSession.getId();
        String shortMessage;

        if (responseAsJSONString.length() < 10) {
            shortMessage = responseAsJSONString;
        } else if (responseAsJSONString.length() < 25) {
            shortMessage = responseAsJSONString.substring(9);
        } else {
            shortMessage = responseAsJSONString.substring(9, 25);
        }
        System.out.println("Sending websocket response " + shortMessage + " to session id " + sessionId);

        MessageHeaders headers = WebsocketCommonUtils.prepareMessageHeaders(sessionId);

        if (websocketSession.isOpen()) {
            messagingTemplate.convertAndSendToUser(sessionId,"/queue/chess_game/response",
                                                                responseAsJSONString, headers);
        }
    }

    public ScheduledFuture<?> getTimeoutDisconnectTask(ChessColor userColor, TimeoutTypeEnum timeoutType) {
        return timeoutDisconnectTasks[getTimeoutDisconnectTaskIndexByColorAndType(userColor, timeoutType)];
    }

    public void setTimeoutDisconnectTask(ChessColor userColor, TimeoutTypeEnum timeoutType, long delay) {
        ScheduledFuture<?> timeoutDisconnectTask = scheduledExecutorService.schedule(new TimeoutGameFinishHandler(userColor,
                                                                timeoutType), delay, TimeUnit.MILLISECONDS);
        timeoutDisconnectTasks[getTimeoutDisconnectTaskIndexByColorAndType(userColor, timeoutType)] = timeoutDisconnectTask;
    }

    private static int getTimeoutDisconnectTaskIndexByColorAndType(ChessColor userColor, TimeoutTypeEnum timeoutType) {
        int index;

        switch (timeoutType) {
            case TIME_IS_UP:
                index = 0;
                break;
            case DISCONNECTED:
                index = 2;
                break;
            default:
                throw new RuntimeException("Incorrect timeout type: " + timeoutType);
        }

        switch (userColor) {
            case WHITE:
                break;
            case BLACK:
                index += 1;
                break;
            default:
                throw new RuntimeException("Incorrect user color: " + userColor);
        }

        return index;
    }

    private long getTimeLeftByColorAndType(ChessColor userColor, TimeoutTypeEnum timeoutType) {
        ScheduledFuture<?> timeoutDisconnectTask = getTimeoutDisconnectTask(userColor, timeoutType);

        if (timeoutDisconnectTask != null && !timeoutDisconnectTask.isDone()) {
            return timeoutDisconnectTask.getDelay(TimeUnit.MILLISECONDS);
        } else {
            switch (timeoutType) {
                case TIME_IS_UP:
                    if (lastMoveNumber < 1) {
                        return ChessGameConstants.FIRST_MOVE_TIME_LEFT_MS;
                    } else {
                        return usersInGameWithOnlineStatusesAndTimings.getTimeLeftByUserColor(userColor);
                    }
                case DISCONNECTED:
                    return -1;
                default:
                    throw new RuntimeException("Incorrect timeout type: " + timeoutType);
            }
        }
    }

    public void checkSubscribed(String username) {
        if (usersSubscribedToGame.get(username) == null) {
            throw new WebsocketException(String.format(NOT_SUBSCRIBED, gameId), WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }
    }

    public void checkFinished() {
        if (finished) {
            throw new WebsocketException(GAME_FINISHED, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }
    }


    @AllArgsConstructor
    private class TimeoutGameFinishHandler implements Runnable {

        private final ChessColor userColor;
        private final TimeoutTypeEnum timeoutType;

        @Override
        public void run() {
            reentrantLock.lock();

            try {
                checkFinished();
                System.out.println("Finished by timeout: " + timeoutType.name());
                ChessGameResult gameResult = gameWebsocketService.finishGameByTimeout(gameId, userColor,
                        timeoutType);
                finishGameWebsockets(gameResult, userColor, timeoutType);
            } finally {
                reentrantLock.unlock();
            }
        }
    }

}