package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindMatchOkResponse extends FindMatchWebsocketResponse {

    private long matchId;

    public FindMatchOkResponse(long matchId) {
        super(FindMatchWebsocketResponseEnum.OK);
        this.matchId = matchId;
    }
}
