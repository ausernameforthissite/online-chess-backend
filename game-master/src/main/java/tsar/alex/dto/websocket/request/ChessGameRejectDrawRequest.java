package tsar.alex.dto.websocket.request;

public class ChessGameRejectDrawRequest extends ChessGameWebsocketRequest {
    public ChessGameRejectDrawRequest() {
        super(ChessGameWebsocketRequestEnum.REJECT_DRAW);
    }
}
