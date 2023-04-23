package tsar.alex.utils.websocket;

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
import tsar.alex.dto.websocket.response.ChessMatchWebsocketResponseEnum.ChessMatchWebsocketBadResponseEnum;
import tsar.alex.exception.WebsocketErrorCodeEnum;
import tsar.alex.exception.WebsocketException;
import tsar.alex.model.*;
import tsar.alex.service.MatchWebsocketService;
import tsar.alex.utils.*;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public class ChessMatchWebsocketRoom {

    public final ReentrantLock reentrantLock = new ReentrantLock();

    private final Map<String, WebsocketSessionWrapper> usersSubscribedToMatch = new ConcurrentHashMap<>(2);
    private final ScheduledFuture<?>[] timeoutFinishers = new ScheduledFuture[4];
    private final String matchId;
    private final UsersInMatchWithOnlineStatusesAndTimings usersInMatchWithOnlineStatusesAndTimings;
    private boolean finished;
    private int lastMoveNumber = -1;
    private int drawOfferMoveNumber = -1;
    private ChessColor drawOfferUserColor;

    private final ChessMatchWebsocketRoomsHolder chessMatchWebsocketRoomsHolder;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduledExecutorService;
    private final MatchWebsocketService matchWebsocketService;


    public void setLastMoveNumber(int lastMoveNumber) {
        this.lastMoveNumber = lastMoveNumber;
    }

    public void addSubscribedUserIfPossible(String username, WebsocketSessionWrapper websocketSessionWrapper) {
        WebsocketSessionWrapper oldWebsocketSessionWrapper = usersSubscribedToMatch.get(username);

        if (oldWebsocketSessionWrapper != null) {
            throw new WebsocketException(ALREADY_SUBSCRIBED, WebsocketErrorCodeEnum.CLOSE_CONNECTION_ALREADY_SUBSCRIBED);
        }
        usersSubscribedToMatch.put(username, websocketSessionWrapper);

        ChessColor userColor = usersInMatchWithOnlineStatusesAndTimings.getColorByUsername(username);

        if (userColor == null) {
            return;
        }

        ScheduledFuture<?> oldTimeoutFinisher = getTimeoutFinisher(userColor, TimeoutTypeEnum.DISCONNECTED);
        if (oldTimeoutFinisher != null) {
            oldTimeoutFinisher.cancel(true);
        }
        usersInMatchWithOnlineStatusesAndTimings.setOnlineStatusByUserColor(userColor, true);

        ChessMatchUserSubscribedResponse userSubscribedResponse = new ChessMatchUserSubscribedResponse(userColor);
        String responseAsJSONString = mapResponseToJSONString(userSubscribedResponse);

        for (Map.Entry<String, WebsocketSessionWrapper> userSessionWrapper : usersSubscribedToMatch.entrySet()) {
            if (!userSessionWrapper.getKey().equals(username)) {
                sendMessageToWebsocketSession(responseAsJSONString, userSessionWrapper.getValue().getSession());
            }
        }
    }

    public void sendInfo(String username) {
        WebsocketSessionWrapper websocketSessionWrapper = usersSubscribedToMatch.get(username);
        ChessMatchInfoResponse response = new ChessMatchInfoResponse(lastMoveNumber,
                                                                    getCurrentUsersOnlineStatusesAndTimings(),
                                                                    ChessGameConstants.BLITZ_INITIAL_TIME_LEFT_MS,
                                                                    ChessGameConstants.FIRST_MOVE_TIME_LEFT_MS,
                                                                    ChessGameConstants.LEFT_GAME_TIMEOUT_MS);

        sendMessageToWebsocketSession(mapResponseToJSONString(response), websocketSessionWrapper.getSession());
    }

    private CurrentUsersOnlineStatusesAndTimings getCurrentUsersOnlineStatusesAndTimings() {
        boolean whiteUserOnline = usersInMatchWithOnlineStatusesAndTimings.isWhiteUserOnline();
        boolean blackUserOnline = usersInMatchWithOnlineStatusesAndTimings.isBlackUserOnline();

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
            whiteTimeLeftMS = ChessGameConstants.BLITZ_INITIAL_TIME_LEFT_MS;
            blackTimeLeftMS = ChessGameConstants.BLITZ_INITIAL_TIME_LEFT_MS;
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

        ChessColor userColor = usersInMatchWithOnlineStatusesAndTimings.getColorByUsername(username);

        if (userColor == null) {
            usersSubscribedToMatch.remove(username);
            return;
        }

        WebsocketSessionWrapper websocketSessionWrapper = usersSubscribedToMatch.get(username);
        if (websocketSessionWrapper == null || !websocketSessionWrapper.getSession().getId().equals(sessionId)) {
            return;
        }

        usersSubscribedToMatch.remove(username);

        usersInMatchWithOnlineStatusesAndTimings.setOnlineStatusByUserColor(userColor, false);

        if (lastMoveNumber >= 1) {
            setTimeoutFinisher(userColor, TimeoutTypeEnum.DISCONNECTED, ChessGameConstants.LEFT_GAME_TIMEOUT_MS);
        }
        ChessMatchUserDisconnectedResponse userDisconnectedResponse = new ChessMatchUserDisconnectedResponse(userColor);
        String responseAsJSONString = mapResponseToJSONString(userDisconnectedResponse);

        for (Map.Entry<String, WebsocketSessionWrapper> userSessionWrapper : usersSubscribedToMatch.entrySet()) {
            sendMessageToWebsocketSession(responseAsJSONString, userSessionWrapper.getValue().getSession());
        }
    }

    public void makeMoveOkResponse(ChessMove chessMove) {
        ChessColor newUserColor = ChessGameUtils.getUserColorByMoveNumber(lastMoveNumber++);
        ChessColor oldUserColor = ChessColor.getInvertedColor(newUserColor);
        Future<?> oldTimeoutFinisher = getTimeoutFinisher(oldUserColor, TimeoutTypeEnum.TIME_IS_UP);
        if (oldTimeoutFinisher != null) {
            oldTimeoutFinisher.cancel(true);
        }

        if (lastMoveNumber >= 1) {
            usersInMatchWithOnlineStatusesAndTimings.setTimeLefts(chessMove);
        }

        if (lastMoveNumber == 1 && !usersInMatchWithOnlineStatusesAndTimings.isWhiteUserOnline()) {
            setTimeoutFinisher(ChessColor.WHITE, TimeoutTypeEnum.DISCONNECTED, ChessGameConstants.LEFT_GAME_TIMEOUT_MS);
        }

        long timeLeft = lastMoveNumber < 1 ? ChessGameConstants.FIRST_MOVE_TIME_LEFT_MS :
                                             chessMove.getTimeLeftByUserColor(newUserColor);
        setTimeoutFinisher(newUserColor, TimeoutTypeEnum.TIME_IS_UP, timeLeft);

        ChessMatchMoveOkResponse chessMatchMoveOkResponse = new ChessMatchMoveOkResponse(chessMove);
        String responseAsJSONString = mapResponseToJSONString(chessMatchMoveOkResponse);

        for (Map.Entry<String, WebsocketSessionWrapper> userSessionWrapper : usersSubscribedToMatch.entrySet()) {
            sendMessageToWebsocketSession(responseAsJSONString, userSessionWrapper.getValue().getSession());
        }
    }

    public void sendBadResponse(@NotNull ChessMatchWebsocketBadResponseEnum badResponseType, String username, String errorMessage) {
        WebsocketSessionWrapper websocketSessionWrapper = usersSubscribedToMatch.get(username); // Can it be null? If it can, it can be NPE two lines below
        String responseAsJSONString = mapResponseToJSONString(new ChessMatchBadResponse(errorMessage, badResponseType));
        sendMessageToWebsocketSession(responseAsJSONString, websocketSessionWrapper.getSession());
    }

    public void makeDrawOffer(String username) {
        ChessColor userColor = usersInMatchWithOnlineStatusesAndTimings.getColorByUsername(username);
        if (userColor == null) {
            sendBadResponse(ChessMatchWebsocketBadResponseEnum.DRAW_BAD, username,
                        "Only players can offer draw!");
            return;
        }

        if (lastMoveNumber < 1) {
            sendBadResponse(ChessMatchWebsocketBadResponseEnum.DRAW_BAD, username,
                    "You can't offer draw before the second turn!");
            return;
        }

        if (lastMoveNumber < drawOfferMoveNumber) {
            sendBadResponse(ChessMatchWebsocketBadResponseEnum.DRAW_BAD, username,
                    "Draw can't be offered twice on the same move.");
            return;
        }

        if (lastMoveNumber == drawOfferMoveNumber && userColor == drawOfferUserColor) {
            sendBadResponse(ChessMatchWebsocketBadResponseEnum.DRAW_BAD, username,
                    "You need to wait one more turn before make another draw offer.");
            return;
        }

        drawOfferMoveNumber = lastMoveNumber + 1;
        drawOfferUserColor = userColor;

        ChessMatchDrawResponse chessMatchDrawResponse = new ChessMatchDrawResponse(userColor);
        String responseAsJSONString = mapResponseToJSONString(chessMatchDrawResponse);

        for (Map.Entry<String, WebsocketSessionWrapper> userSessionWrapper : usersSubscribedToMatch.entrySet()) {
            if (!userSessionWrapper.getKey().equals(username)) {
                sendMessageToWebsocketSession(responseAsJSONString, userSessionWrapper.getValue().getSession());
            }
        }
    }

    public void rejectDrawOffer(String username) {
        checkDrawActionPossibility(username, ChessMatchWebsocketBadResponseEnum.REJECT_DRAW_BAD);

        drawOfferMoveNumber = -1;
        ChessMatchRejectDrawResponse rejectDrawResponse = new ChessMatchRejectDrawResponse();
        String responseAsJSONString = mapResponseToJSONString(rejectDrawResponse);

        for (Map.Entry<String, WebsocketSessionWrapper> userSessionWrapper : usersSubscribedToMatch.entrySet()) {
            if (!userSessionWrapper.getKey().equals(username)) {
                sendMessageToWebsocketSession(responseAsJSONString, userSessionWrapper.getValue().getSession());
            }
        }
    }

    public void acceptDrawOffer(String username) {
        checkDrawActionPossibility(username, ChessMatchWebsocketBadResponseEnum.ACCEPT_DRAW_BAD);

        ChessMatchResult matchResult = new ChessMatchResult();
        matchResult.setDraw(true);
        matchResult.setMessage("The players agreed to a draw.");
        finishMatchWebsockets(matchWebsocketService.finishMatch(matchId, matchResult));
    }

    private void checkDrawActionPossibility(String username, ChessMatchWebsocketBadResponseEnum drawBadResponseType) {
        if (drawOfferUserColor == null || lastMoveNumber > drawOfferMoveNumber) {
            sendBadResponse(drawBadResponseType, username, "Draw offer doesn't exist or is expired!");
            return;
        }

        ChessColor userColor = usersInMatchWithOnlineStatusesAndTimings.getColorByUsername(username);
        ChessColor userToWhomDrawWasOffered = ChessColor.getInvertedColor(drawOfferUserColor);

        if (userColor != userToWhomDrawWasOffered) {
            sendBadResponse(drawBadResponseType, username, "Only " +
                    userToWhomDrawWasOffered.name().toLowerCase() + " color user can accept/reject draw offer!");
        }
    }

    public void surrender(String username) {
        ChessColor userColor = usersInMatchWithOnlineStatusesAndTimings.getColorByUsername(username);

        if (userColor == null) {
            sendBadResponse(ChessMatchWebsocketBadResponseEnum.SURRENDER_BAD, username,
                    "Only players can surrender!");
        }

        ChessMatchResult matchResult = new ChessMatchResult();
        matchResult.setWinnerColor(ChessColor.getInvertedColor(userColor));
        matchResult.setMessage((userColor == ChessColor.WHITE ? "White" : "Black") + " player has surrendered");
        finishMatchWebsockets(matchWebsocketService.finishMatch(matchId, matchResult));
    }

    public void finishMatchWebsockets(ChessMatchResult matchResult) {
        finishMatchWebsockets(matchResult, null);
    }

    public void finishMatchWebsockets(ChessMatchResult matchResult, ChessColor userColor, TimeoutTypeEnum timeoutType) {
        finishMatchWebsockets(matchResult, getTimeoutFinisher(userColor, timeoutType));
    }

    private void finishMatchWebsockets(ChessMatchResult matchResult, Future<?> currentTimeoutFinisher) {
        chessMatchWebsocketRoomsHolder.removeMatchWebsocketRoom(matchId);

        for (Future<?> timeoutFinisher : timeoutFinishers) {
            if (timeoutFinisher != null && timeoutFinisher != currentTimeoutFinisher) {
                timeoutFinisher.cancel(true);
            }
        }

        ChessMatchResultResponse matchResultResponse = new ChessMatchResultResponse(matchResult);
        String responseAsJSONString = mapResponseToJSONString(matchResultResponse);

        for (Map.Entry<String, WebsocketSessionWrapper> userSessionWrapper : usersSubscribedToMatch.entrySet()) {
            WebsocketSessionWrapper websocketSessionWrapper = userSessionWrapper.getValue();
            WebSocketSession websocketSession = websocketSessionWrapper.getSession();
            sendMessageToWebsocketSession(responseAsJSONString, websocketSession);

            ScheduledFuture<?> timeoutFinisher = scheduledExecutorService.schedule(
                                                    new TimeoutWebsocketCloseHandler(websocketSession),
                                                    Constants.SESSION_TO_BE_CLOSED_TIMEOUT_MS,
                                                    TimeUnit.MILLISECONDS);
            websocketSessionWrapper.setTimeoutFinisher(timeoutFinisher);
        }

        finished = true;
    }

    private String mapResponseToJSONString(ChessMatchWebsocketResponse response) {
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
            messagingTemplate.convertAndSendToUser(sessionId,"/queue/chess_match/response",
                                                                responseAsJSONString, headers);
        }
    }

    public ScheduledFuture<?> getTimeoutFinisher(ChessColor userColor, TimeoutTypeEnum timeoutType) {
        return timeoutFinishers[getTimeoutFinisherIndexByColorAndType(userColor, timeoutType)];
    }

    public void setTimeoutFinisher(ChessColor userColor, TimeoutTypeEnum timeoutType, long delay) {
        ScheduledFuture<?> timeoutFinisher = scheduledExecutorService.schedule(new TimeoutMatchFinishHandler(userColor,
                                                                timeoutType), delay, TimeUnit.MILLISECONDS);
        timeoutFinishers[getTimeoutFinisherIndexByColorAndType(userColor, timeoutType)] = timeoutFinisher;
    }

    private static int getTimeoutFinisherIndexByColorAndType(ChessColor userColor, TimeoutTypeEnum timeoutType) {
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
        ScheduledFuture<?> timeoutFinisher = getTimeoutFinisher(userColor, timeoutType);

        if (timeoutFinisher != null && !timeoutFinisher.isDone()) {
            return timeoutFinisher.getDelay(TimeUnit.MILLISECONDS);
        } else {
            switch (timeoutType) {
                case TIME_IS_UP:
                    if (lastMoveNumber < 1) {
                        return ChessGameConstants.FIRST_MOVE_TIME_LEFT_MS;
                    } else {
                        return usersInMatchWithOnlineStatusesAndTimings.getTimeLeftByUserColor(userColor);
                    }
                case DISCONNECTED:
                    return -1;
                default:
                    throw new RuntimeException("Incorrect timeout type: " + timeoutType);
            }
        }
    }

    public void checkSubscribed(String username) {
        if (usersSubscribedToMatch.get(username) == null) {
            throw new WebsocketException(String.format(NOT_SUBSCRIBED, matchId), WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }
    }

    public void checkFinished() {
        if (finished) {
            throw new WebsocketException(MATCH_FINISHED, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }
    }


    @AllArgsConstructor
    private class TimeoutMatchFinishHandler implements Runnable {

        private final ChessColor userColor;
        private final TimeoutTypeEnum timeoutType;

        @Override
        public void run() {
            reentrantLock.lock();

            try {
                checkFinished();
                System.out.println("Finished by timeout: " + timeoutType.name());
                ChessMatchResult matchResult = matchWebsocketService.finishMatchByTimeout(matchId, userColor,
                        timeoutType);
                finishMatchWebsockets(matchResult, userColor, timeoutType);
            } finally {
                reentrantLock.unlock();
            }
        }
    }

}