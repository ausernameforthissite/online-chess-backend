package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.ChessColor;

@Getter
@Setter
public class ChessGameUserDisconnectedResponse extends ChessGameWebsocketResponse {
    private ChessColor disconnectedUserColor;

    public ChessGameUserDisconnectedResponse(ChessColor disconnectedUserColor) {
        super(ChessGameWebsocketResponseEnum.DISCONNECTED);
        this.disconnectedUserColor = disconnectedUserColor;
    }
}