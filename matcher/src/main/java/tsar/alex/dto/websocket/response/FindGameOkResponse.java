package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindGameOkResponse extends FindGameWebsocketResponse {

    private String gameId;

    public FindGameOkResponse(String gameId) {
        super(FindGameWebsocketResponseEnum.OK);
        this.gameId = gameId;
    }
}
