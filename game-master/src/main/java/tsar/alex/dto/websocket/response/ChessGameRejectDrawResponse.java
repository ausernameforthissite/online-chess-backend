package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChessGameRejectDrawResponse extends ChessGameWebsocketResponse {
    public ChessGameRejectDrawResponse() {
        super(ChessGameWebsocketResponseEnum.REJECT_DRAW);
    }
}
