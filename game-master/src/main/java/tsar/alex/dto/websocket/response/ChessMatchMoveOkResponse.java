package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.ChessMove;

@Getter
@Setter
public class ChessMatchMoveOkResponse extends ChessMatchWebsocketResponse {

    private ChessMove chessMove;

    public ChessMatchMoveOkResponse(ChessMove chessMove) {
        super(ChessMatchWebsocketResponseEnum.CHESS_MOVE);
        this.chessMove = chessMove;
    }
}
