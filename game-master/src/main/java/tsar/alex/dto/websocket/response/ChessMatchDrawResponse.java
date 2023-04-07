package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.ChessColor;

@Getter
@Setter
public class ChessMatchDrawResponse extends ChessMatchWebsocketResponse {
    private ChessColor drawOfferUserColor;

    public ChessMatchDrawResponse(ChessColor drawOfferUserColor) {
        super(ChessMatchWebsocketResponseEnum.DRAW);
        this.drawOfferUserColor = drawOfferUserColor;
    }
}
