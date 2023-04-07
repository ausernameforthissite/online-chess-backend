package tsar.alex.dto.websocket.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChessMatchSurrenderRequest extends ChessMatchWebsocketRequest {

    public ChessMatchSurrenderRequest() {
        super(ChessMatchWebsocketRequestEnum.SURRENDER);
    }
}
