package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.ChessColor;

@Getter
@Setter
public class ChessGameDrawResponse extends ChessGameWebsocketResponse {
    private ChessColor drawOfferUserColor;

    public ChessGameDrawResponse(ChessColor drawOfferUserColor) {
        super(ChessGameWebsocketResponseEnum.DRAW);
        this.drawOfferUserColor = drawOfferUserColor;
    }
}
