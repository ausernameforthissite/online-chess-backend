package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.ChessMove;

@Getter
@Setter
public class ChessGameMoveOkResponse extends ChessGameWebsocketResponse {

    private ChessMove chessMove;

    public ChessGameMoveOkResponse(ChessMove chessMove) {
        super(ChessGameWebsocketResponseEnum.CHESS_MOVE);
        this.chessMove = chessMove;
    }
}
