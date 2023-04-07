package tsar.alex.dto.websocket.response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChessMatchBadResponse extends ChessMatchWebsocketResponse {

    private String message;

    public ChessMatchBadResponse(String message, ChessMatchWebsocketResponseEnum badResponseType) {
        super(badResponseType);
        this.message = message;
    }
}
