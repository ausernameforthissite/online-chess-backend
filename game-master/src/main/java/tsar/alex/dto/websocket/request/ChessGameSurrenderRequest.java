package tsar.alex.dto.websocket.request;



public class ChessGameSurrenderRequest extends ChessGameWebsocketRequest {
    public ChessGameSurrenderRequest() {
        super(ChessGameWebsocketRequestEnum.SURRENDER);
    }
}
