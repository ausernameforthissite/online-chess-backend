package tsar.alex.dto.websocket.request;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.ChessMove;

@Getter
@Setter
public class ChessMatchMoveRequest extends ChessMatchWebsocketRequest {
    @NotNull(message = "chessMove is null")
    private ChessMove chessMove;

    public ChessMatchMoveRequest() {
        super(ChessMatchWebsocketRequestEnum.CHESS_MOVE);
    }
}
