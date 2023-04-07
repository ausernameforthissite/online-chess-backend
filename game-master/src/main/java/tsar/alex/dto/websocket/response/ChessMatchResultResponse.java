package tsar.alex.dto.websocket.response;

import lombok.Getter;
import lombok.Setter;
import tsar.alex.model.ChessMatchResult;

@Getter
@Setter
public class ChessMatchResultResponse extends ChessMatchWebsocketResponse {
    private ChessMatchResult matchResult;

    public ChessMatchResultResponse(ChessMatchResult matchResult) {
        super(ChessMatchWebsocketResponseEnum.MATCH_RESULT);
        this.matchResult = matchResult;
    }
}
