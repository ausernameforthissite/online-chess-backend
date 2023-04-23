package tsar.alex.controller;

import static tsar.alex.utils.CommonTextConstants.INCORRECT_JSON;
import static tsar.alex.utils.CommonTextConstants.INCORRECT_WEBSOCKET_CONTENT;
import static tsar.alex.utils.CommonTextConstants.UNKNOWN_ERROR;

import lombok.AllArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Controller;
import tsar.alex.dto.websocket.request.ChessMatchMoveRequest;
import tsar.alex.dto.websocket.request.ChessMatchWebsocketRequest;
import tsar.alex.dto.websocket.request.ChessMatchWebsocketRequestEnum;
import tsar.alex.dto.websocket.response.ChessMatchBadResponse;
import tsar.alex.dto.websocket.response.ChessMatchWebsocketResponseEnum.ChessMatchWebsocketBadResponseEnum;
import tsar.alex.service.MatchService;

import java.security.Principal;
import tsar.alex.utils.WebsocketCommonUtils;

@Controller
@AllArgsConstructor
public class MatchWebsocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MatchService matchService;

    @MessageMapping("/chess_match/request")
    public void handleChessMatchWebsocketMessage(ChessMatchWebsocketRequest request, Principal user) {

        String matchId = (String) ((AbstractAuthenticationToken) user).getDetails();

        ChessMatchWebsocketRequestEnum requestType = request.getType();

        switch (requestType) {
            case CHESS_MOVE -> matchService.makeMove(matchId, ((ChessMatchMoveRequest) request).getChessMove());
            case INFO, DRAW, ACCEPT_DRAW, REJECT_DRAW, SURRENDER -> matchService.handleUserMatchRequest(matchId, requestType);
            default -> throw new RuntimeException();
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

        ChessMatchBadResponse badResponse = new ChessMatchBadResponse(message, ChessMatchWebsocketBadResponseEnum.GENERAL_BAD);
        messagingTemplate.convertAndSendToUser(sessionId,"/queue/chess_match/response", badResponse, headers);
    }
}
