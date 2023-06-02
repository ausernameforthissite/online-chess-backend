package tsar.alex.dto.websocket.response;


public enum ChessGameWebsocketResponseEnum {
    INFO,
    SUBSCRIBED,
    DISCONNECTED,
    CHESS_MOVE,
    DRAW,
    REJECT_DRAW,
    GAME_RESULT,
    GENERAL_BAD,
    CHESS_MOVE_BAD,
    DRAW_BAD,
    REJECT_DRAW_BAD,
    ACCEPT_DRAW_BAD,
    SURRENDER_BAD;

    public enum ChessGameWebsocketBadResponseEnum {
        GENERAL_BAD,
        CHESS_MOVE_BAD,
        DRAW_BAD,
        REJECT_DRAW_BAD,
        ACCEPT_DRAW_BAD,
        SURRENDER_BAD;

        public ChessGameWebsocketResponseEnum toChessGameWebsocketResponseEnum() {
            switch (this) {
                case GENERAL_BAD:
                    return ChessGameWebsocketResponseEnum.GENERAL_BAD;
                case CHESS_MOVE_BAD:
                    return ChessGameWebsocketResponseEnum.CHESS_MOVE_BAD;
                case DRAW_BAD:
                    return ChessGameWebsocketResponseEnum.DRAW_BAD;
                case REJECT_DRAW_BAD:
                    return ChessGameWebsocketResponseEnum.REJECT_DRAW_BAD;
                case ACCEPT_DRAW_BAD:
                    return ChessGameWebsocketResponseEnum.ACCEPT_DRAW_BAD;
                case SURRENDER_BAD:
                    return ChessGameWebsocketResponseEnum.SURRENDER_BAD;
            }
            throw new RuntimeException("Incorrect type ChessGameWebsocketResponseEnum value: " + this.name());
        }
    }
}

