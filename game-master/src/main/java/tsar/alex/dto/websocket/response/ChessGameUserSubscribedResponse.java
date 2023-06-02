package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.ChessColor;

@Getter
@Setter
public class ChessGameUserSubscribedResponse extends ChessGameWebsocketResponse {
    private ChessColor subscribedUserColor;

    public ChessGameUserSubscribedResponse(ChessColor subscribedUserColor) {
        super(ChessGameWebsocketResponseEnum.SUBSCRIBED);
        this.subscribedUserColor = subscribedUserColor;
    }
}