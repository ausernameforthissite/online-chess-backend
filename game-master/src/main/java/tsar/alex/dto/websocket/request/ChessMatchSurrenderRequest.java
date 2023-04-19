package tsar.alex.dto.websocket.request;



public class ChessMatchSurrenderRequest extends ChessMatchWebsocketRequest {
    public ChessMatchSurrenderRequest() {
        super(ChessMatchWebsocketRequestEnum.SURRENDER);
    }
}
