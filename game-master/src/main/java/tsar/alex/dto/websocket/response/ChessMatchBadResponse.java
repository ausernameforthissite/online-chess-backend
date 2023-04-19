package tsar.alex.dto.websocket.response;


import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tsar.alex.dto.websocket.response.ChessMatchWebsocketResponseEnum.ChessMatchWebsocketBadResponseEnum;

@Getter
@Setter
public class ChessMatchBadResponse extends ChessMatchWebsocketResponse {

    private String message;

    public ChessMatchBadResponse(String message, @NotNull ChessMatchWebsocketBadResponseEnum badResponseType) {
        super(badResponseType.toChessMatchWebsocketResponseEnum());
        this.message = message;
    }
}
