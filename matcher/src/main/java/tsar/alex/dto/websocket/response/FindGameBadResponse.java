package tsar.alex.dto.websocket.response;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tsar.alex.dto.websocket.response.FindGameWebsocketResponseEnum.FindGameWebsocketBadResponseEnum;

@Getter
@Setter
public class FindGameBadResponse extends FindGameWebsocketResponse {

    private String message;

    public FindGameBadResponse(String message, @NotNull FindGameWebsocketBadResponseEnum badResponseType) {
        super(badResponseType.toFindGameWebsocketResponseEnum());
        this.message = message;
    }
}
