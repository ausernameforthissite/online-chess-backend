package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.ChessColor;

@Getter
@Setter
public class ChessMatchUserDisconnectedResponse extends ChessMatchWebsocketResponse {
    private ChessColor disconnectedUserColor;

    public ChessMatchUserDisconnectedResponse(ChessColor disconnectedUserColor) {
        super(ChessMatchWebsocketResponseEnum.DISCONNECTED);
        this.disconnectedUserColor = disconnectedUserColor;
    }
}