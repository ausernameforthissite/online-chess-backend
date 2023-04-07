package tsar.alex.dto.websocket.request;


public class ChessMatchDrawRequest extends ChessMatchWebsocketRequest {
    public ChessMatchDrawRequest() {
        super(ChessMatchWebsocketRequestEnum.DRAW);
    }
}
