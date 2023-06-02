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
import tsar.alex.dto.websocket.request.FindGameWebsocketRequest;
import tsar.alex.dto.websocket.response.FindGameBadResponse;
import tsar.alex.dto.websocket.response.FindGameWebsocketResponseEnum.FindGameWebsocketBadResponseEnum;
import tsar.alex.utils.WebsocketCommonUtils;
import tsar.alex.api.websocket.UsersWaitingForGameWebsocketHolder;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class FindGameWebsocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final UsersWaitingForGameWebsocketHolder uwfgWebsocketHolder;

    @MessageMapping("/find_game/request")
    public void handleFindGameWebsocketMessage(FindGameWebsocketRequest request, Principal user) {
        uwfgWebsocketHolder.cancelSearchIfPossible(user.getName());
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
            e.printStackTrace();
        }

        String sessionId = accessor.getSessionId();
        MessageHeaders headers = WebsocketCommonUtils.prepareMessageHeaders(sessionId);
        FindGameBadResponse badResponse = new FindGameBadResponse(message, FindGameWebsocketBadResponseEnum.GENERAL_BAD);
        messagingTemplate.convertAndSendToUser(sessionId,"/queue/find_game/response", badResponse, headers);
    }
}