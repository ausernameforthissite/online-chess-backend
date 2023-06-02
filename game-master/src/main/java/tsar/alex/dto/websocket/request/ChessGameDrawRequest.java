package tsar.alex.dto.websocket.request;


public class ChessGameDrawRequest extends ChessGameWebsocketRequest {
    public ChessGameDrawRequest() {
        super(ChessGameWebsocketRequestEnum.DRAW);
    }
}
