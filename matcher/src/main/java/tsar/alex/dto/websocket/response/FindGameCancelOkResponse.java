package tsar.alex.dto.websocket.response;


public class FindGameCancelOkResponse extends FindGameWebsocketResponse {

    public FindGameCancelOkResponse() {
        super(FindGameWebsocketResponseEnum.CANCELED);
    }

}
