package tsar.alex.dto.websocket.response;


import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tsar.alex.dto.websocket.response.ChessGameWebsocketResponseEnum.ChessGameWebsocketBadResponseEnum;

@Getter
@Setter
public class ChessGameBadResponse extends ChessGameWebsocketResponse {

    private String message;

    public ChessGameBadResponse(String message, @NotNull ChessGameWebsocketBadResponseEnum badResponseType) {
        super(badResponseType.toChessGameWebsocketResponseEnum());
        this.message = message;
    }
}
