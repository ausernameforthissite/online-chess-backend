package tsar.alex.dto.websocket.response;


public enum ChessMatchWebsocketResponseEnum {
    INFO,
    SUBSCRIBED,
    DISCONNECTED,
    CHESS_MOVE,
    DRAW,
    REJECT_DRAW,
    MATCH_RESULT,
    GENERAL_BAD,
    CHESS_MOVE_BAD,
    DRAW_BAD,
    REJECT_DRAW_BAD,
    ACCEPT_DRAW_BAD,
    SURRENDER_BAD;

    public enum ChessMatchWebsocketBadResponseEnum {
        GENERAL_BAD,
        CHESS_MOVE_BAD,
        DRAW_BAD,
        REJECT_DRAW_BAD,
        ACCEPT_DRAW_BAD,
        SURRENDER_BAD;

        public ChessMatchWebsocketResponseEnum toChessMatchWebsocketResponseEnum() {
            switch (this) {
                case GENERAL_BAD:
                    return ChessMatchWebsocketResponseEnum.GENERAL_BAD;
                case CHESS_MOVE_BAD:
                    return ChessMatchWebsocketResponseEnum.CHESS_MOVE_BAD;
                case DRAW_BAD:
                    return ChessMatchWebsocketResponseEnum.DRAW_BAD;
                case REJECT_DRAW_BAD:
                    return ChessMatchWebsocketResponseEnum.REJECT_DRAW_BAD;
                case ACCEPT_DRAW_BAD:
                    return ChessMatchWebsocketResponseEnum.ACCEPT_DRAW_BAD;
                case SURRENDER_BAD:
                    return ChessMatchWebsocketResponseEnum.SURRENDER_BAD;
            }
            throw new RuntimeException("Incorrect type ChessMatchWebsocketResponseEnum value: " + this.name());
        }
    }
}

