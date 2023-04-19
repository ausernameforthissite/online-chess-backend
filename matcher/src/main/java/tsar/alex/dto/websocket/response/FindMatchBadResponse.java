package tsar.alex.dto.websocket.response;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tsar.alex.dto.websocket.response.FindMatchWebsocketResponseEnum.FindMatchWebsocketBadResponseEnum;

@Getter
@Setter
public class FindMatchBadResponse extends FindMatchWebsocketResponse {

    private String message;

    public FindMatchBadResponse(String message, @NotNull FindMatchWebsocketBadResponseEnum badResponseType) {
        super(badResponseType.toFindMatchWebsocketResponseEnum());
        this.message = message;
    }
}
