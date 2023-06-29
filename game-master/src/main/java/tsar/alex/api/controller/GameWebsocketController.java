package tsar.alex.api.controller;

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
import tsar.alex.dto.websocket.request.ChessGameMoveRequest;
import tsar.alex.dto.websocket.request.ChessGameWebsocketRequest;
import tsar.alex.dto.websocket.request.ChessGameWebsocketRequestEnum;
import tsar.alex.dto.websocket.response.ChessGameBadResponse;
import tsar.alex.dto.websocket.response.ChessGameWebsocketResponseEnum.ChessGameWebsocketBadResponseEnum;
import tsar.alex.service.GameService;

import java.security.Principal;
import tsar.alex.utils.WebsocketCommonUtils;

@Controller
@AllArgsConstructor
public class GameWebsocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;

    @MessageMapping("/chess_game/request")
    public void handleChessGameWebsocketMessage(ChessGameWebsocketRequest request, Principal user) {

        String gameId = (String) ((AbstractAuthenticationToken) user).getDetails();

        ChessGameWebsocketRequestEnum requestType = request.getType();

        switch (requestType) {
            case CHESS_MOVE -> gameService.makeMove(gameId, ((ChessGameMoveRequest) request).getChessMove());
            case INFO, DRAW, ACCEPT_DRAW, REJECT_DRAW, SURRENDER -> gameService.handleUserGameRequest(gameId, requestType);
            default -> throw new RuntimeException();
        }
    }

    @MessageExceptionHandler({Exception.class})
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

        ChessGameBadResponse badResponse = new ChessGameBadResponse(message, ChessGameWebsocketBadResponseEnum.GENERAL_BAD);
        messagingTemplate.convertAndSendToUser(sessionId,"/queue/chess_game/response", badResponse, headers);
    }
}
