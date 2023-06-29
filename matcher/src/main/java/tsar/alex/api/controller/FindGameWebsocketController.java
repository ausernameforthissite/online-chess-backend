package tsar.alex.api.controller;

import static tsar.alex.utils.CommonTextConstants.*;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import tsar.alex.api.WebsocketMessageSender;
import tsar.alex.dto.websocket.request.FindGameCancelRequest;
import tsar.alex.dto.websocket.request.FindGameWebsocketRequest;
import tsar.alex.dto.websocket.response.FindGameBadResponse;
import tsar.alex.dto.websocket.response.FindGameWebsocketResponseEnum.FindGameWebsocketBadResponseEnum;
import tsar.alex.exception.UnexpectedObjectClassException;
import tsar.alex.model.WebsocketSessionMap;
import tsar.alex.utils.WebsocketCommonUtils;
import tsar.alex.websocket.UsersWaitingForGameWebsocketHolder;

import java.security.Principal;

@Controller
@AllArgsConstructor
@Slf4j
public class FindGameWebsocketController {

    private final UsersWaitingForGameWebsocketHolder uwfgWebsocketHolder;
    private final WebsocketMessageSender websocketMessageSender;
    private final WebsocketSessionMap websocketSessions;

    @MessageMapping("/find_game/request")
    public void handleFindGameWebsocketMessage(FindGameWebsocketRequest request, Principal user) {
        if (request instanceof FindGameCancelRequest) {
            uwfgWebsocketHolder.cancelSearchIfPossible(user.getName());
        } else {
            throw new UnexpectedObjectClassException(
                    String.format(UNEXPECTED_OBJECT_CLASS, "request", request.getClass()));
        }
    }

    @MessageExceptionHandler({Exception.class})
    public void methodArgumentNotValidWebSocketExceptionHandler(Exception e, StompHeaderAccessor accessor) {
        String message;
        if (e instanceof MethodArgumentNotValidException) {
            message = INCORRECT_WEBSOCKET_CONTENT;
        } else if (e instanceof MessageConversionException) {
            message = INCORRECT_JSON;
        } else {
            message = UNKNOWN_ERROR;
        }

        String sessionId = Objects.requireNonNull(accessor.getSessionId());
        log.error(String.format(WEBSOCKET_REQUEST_HANDLING_EXCEPTION_LOG, sessionId, e.toString()));
        FindGameBadResponse badResponse = new FindGameBadResponse(message,
                FindGameWebsocketBadResponseEnum.GENERAL_BAD);
        websocketMessageSender.sendMessageToWebsocketSession(badResponse,
                websocketSessions.get(sessionId).getSession());
    }
}