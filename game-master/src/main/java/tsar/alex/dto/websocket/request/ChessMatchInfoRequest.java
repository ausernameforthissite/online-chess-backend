package tsar.alex.dto.websocket.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChessMatchInfoRequest extends ChessMatchWebsocketRequest {
    public ChessMatchInfoRequest() {
        super(ChessMatchWebsocketRequestEnum.INFO);
    }
}
