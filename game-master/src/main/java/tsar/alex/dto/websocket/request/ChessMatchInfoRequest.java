package tsar.alex.dto.websocket.request;




public class ChessMatchInfoRequest extends ChessMatchWebsocketRequest {
    public ChessMatchInfoRequest() {
        super(ChessMatchWebsocketRequestEnum.INFO);
    }
}
