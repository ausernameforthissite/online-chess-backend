package tsar.alex.dto.websocket.request;


public class FindMatchCancelRequest extends FindMatchWebsocketRequest {
    public FindMatchCancelRequest() {
        super(FindMatchWebsocketRequestEnum.CANCEL);
    }
}
