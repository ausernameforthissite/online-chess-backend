package tsar.alex.dto.websocket.response;


public class FindMatchCancelOkResponse extends FindMatchWebsocketResponse {

    public FindMatchCancelOkResponse() {
        super(FindMatchWebsocketResponseEnum.CANCELED);
    }
}
