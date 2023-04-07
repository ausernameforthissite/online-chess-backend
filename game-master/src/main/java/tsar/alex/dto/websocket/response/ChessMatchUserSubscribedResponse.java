package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.ChessColor;

@Getter
@Setter
public class ChessMatchUserSubscribedResponse extends ChessMatchWebsocketResponse {
    private ChessColor subscribedUserColor;

    public ChessMatchUserSubscribedResponse(ChessColor subscribedUserColor) {
        super(ChessMatchWebsocketResponseEnum.SUBSCRIBED);
        this.subscribedUserColor = subscribedUserColor;
    }
}