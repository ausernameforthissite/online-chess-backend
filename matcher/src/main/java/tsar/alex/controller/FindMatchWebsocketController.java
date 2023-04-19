package tsar.alex.controller;

import static tsar.alex.utils.CommonTextConstants.*;

import lombok.AllArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import tsar.alex.dto.websocket.request.FindMatchCancelRequest;
import tsar.alex.dto.websocket.request.FindMatchWebsocketRequest;
import tsar.alex.dto.websocket.response.FindMatchBadResponse;
import tsar.alex.dto.websocket.response.FindMatchWebsocketResponseEnum.FindMatchWebsocketBadResponseEnum;
import tsar.alex.utils.WebsocketCommonUtils;
import tsar.alex.utils.websocket.UsersWaitingForMatchWebsocketHolder;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class FindMatchWebsocketController {
    private static final String REQUEST_OF_UNKNOWN_TYPE = "Find match websocket request of unknown type";

    private final SimpMessagingTemplate messagingTemplate;
    private final UsersWaitingForMatchWebsocketHolder UWFMWebsocketHolder;

    @MessageMapping("/find_match/request")
    public void handleFindMatchWebsocketMessage(FindMatchWebsocketRequest request, Principal user) {
        if (request instanceof FindMatchCancelRequest) {
            UWFMWebsocketHolder.cancelSearchIfPossible(user.getName());
        } else {
            throw new RuntimeException(REQUEST_OF_UNKNOWN_TYPE);
        }
    }


    @MessageExceptionHandler({MethodArgumentNotValidException.class, MessageConversionException.class, Exception.class})
    public void methodArgumentNotValidWebSocketExceptionHandler(Exception e, StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        MessageHeaders headers = WebsocketCommonUtils.prepareMessageHeaders(sessionId);

        String message;
        if (e instanceof MethodArgumentNotValidException) {
            message = INCORRECT_WEBSOCKET_CONTENT;
        } else if (e instanceof MessageConversionException) {
            message = INCORRECT_JSON;
        } else {
            message = UNKNOWN_ERROR;
            e.printStackTrace();
        }

        FindMatchBadResponse badResponse = new FindMatchBadResponse(message, FindMatchWebsocketBadResponseEnum.GENERAL_BAD);
        messagingTemplate.convertAndSendToUser(sessionId,"/queue/find_match/response", badResponse, headers);
    }
}