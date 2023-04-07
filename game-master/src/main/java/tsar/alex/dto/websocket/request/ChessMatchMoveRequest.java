package tsar.alex.dto.websocket.request;

import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.ChessMove;

@Getter
@Setter
public class ChessMatchMoveRequest extends ChessMatchWebsocketRequest {
    private ChessMove chessMove;

    public ChessMatchMoveRequest() {
        super(ChessMatchWebsocketRequestEnum.CHESS_MOVE);
    }
}
