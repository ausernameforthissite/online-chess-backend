package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.ChessGameResult;

@Getter
@Setter
public class ChessGameResultResponse extends ChessGameWebsocketResponse {

    private ChessGameResult gameResult;

    public ChessGameResultResponse(ChessGameResult GameResult) {
        super(ChessGameWebsocketResponseEnum.GAME_RESULT);
        this.gameResult = GameResult;
    }
}
