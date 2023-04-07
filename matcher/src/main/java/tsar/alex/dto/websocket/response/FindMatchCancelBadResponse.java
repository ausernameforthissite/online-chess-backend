package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindMatchCancelBadResponse extends FindMatchWebsocketResponse {

    private String message;

    public FindMatchCancelBadResponse(String message) {
        super(FindMatchWebsocketResponseEnum.CANCEL_FAILED);
        this.message = message;
    }
}

