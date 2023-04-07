package tsar.alex.controller;

import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Controller;
import tsar.alex.dto.websocket.request.ChessMatchMoveRequest;
import tsar.alex.dto.websocket.request.ChessMatchWebsocketRequest;
import tsar.alex.dto.websocket.request.ChessMatchWebsocketRequestEnum;
import tsar.alex.exception.ChessMatchWebsocketException;
import tsar.alex.service.MatchService;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class MatchWebsocketController {

    private final MatchService matchService;

    @MessageMapping("/chess_match/request")
    public void handleChessMatchWebsocketMessage(ChessMatchWebsocketRequest request, Principal user) {

        long matchId = (long) ((AbstractAuthenticationToken) user).getDetails();

        ChessMatchWebsocketRequestEnum requestType = request.getType();

        switch (requestType) {
            case CHESS_MOVE -> matchService.makeMove(matchId, ((ChessMatchMoveRequest) request).getChessMove());
            case INFO, DRAW, ACCEPT_DRAW, REJECT_DRAW, SURRENDER -> matchService.handleUserMatchRequest(matchId, requestType);
            default -> throw new ChessMatchWebsocketException("Incorrect request type: " + request.getType());
        }
    }
}
