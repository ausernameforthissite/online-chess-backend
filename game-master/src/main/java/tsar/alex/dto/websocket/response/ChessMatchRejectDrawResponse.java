package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChessMatchRejectDrawResponse extends ChessMatchWebsocketResponse {
    public ChessMatchRejectDrawResponse() {
        super(ChessMatchWebsocketResponseEnum.REJECT_DRAW);
    }
}
