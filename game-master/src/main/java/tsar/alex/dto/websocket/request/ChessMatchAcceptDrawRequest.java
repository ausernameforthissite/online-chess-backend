package tsar.alex.dto.websocket.request;

public class ChessMatchAcceptDrawRequest extends ChessMatchWebsocketRequest {
    public ChessMatchAcceptDrawRequest() {
        super(ChessMatchWebsocketRequestEnum.ACCEPT_DRAW);
    }
}
