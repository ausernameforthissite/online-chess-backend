package tsar.alex.dto.websocket.request;




public class ChessGameInfoRequest extends ChessGameWebsocketRequest {
    public ChessGameInfoRequest() {
        super(ChessGameWebsocketRequestEnum.INFO);
    }
}
