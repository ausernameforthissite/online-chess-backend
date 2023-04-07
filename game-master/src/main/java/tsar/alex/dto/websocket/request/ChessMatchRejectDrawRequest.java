package tsar.alex.dto.websocket.request;

public class ChessMatchRejectDrawRequest extends ChessMatchWebsocketRequest {
    public ChessMatchRejectDrawRequest() {
        super(ChessMatchWebsocketRequestEnum.REJECT_DRAW);
    }
}
