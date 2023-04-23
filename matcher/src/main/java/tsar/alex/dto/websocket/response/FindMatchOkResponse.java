package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindMatchOkResponse extends FindMatchWebsocketResponse {

    private String matchId;

    public FindMatchOkResponse(String matchId) {
        super(FindMatchWebsocketResponseEnum.OK);
        this.matchId = matchId;
    }
}
