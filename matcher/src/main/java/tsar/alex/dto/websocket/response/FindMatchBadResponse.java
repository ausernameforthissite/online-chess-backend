package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindMatchBadResponse extends FindMatchWebsocketResponse {

    private String message;

    public FindMatchBadResponse(String message) {
        super(FindMatchWebsocketResponseEnum.BAD);
        this.message = message;
    }
}
