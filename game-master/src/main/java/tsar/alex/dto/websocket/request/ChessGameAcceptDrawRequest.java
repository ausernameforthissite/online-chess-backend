package tsar.alex.dto.websocket.request;

public class ChessGameAcceptDrawRequest extends ChessGameWebsocketRequest {
    public ChessGameAcceptDrawRequest() {
        super(ChessGameWebsocketRequestEnum.ACCEPT_DRAW);
    }
}
