package tsar.alex.dto.websocket.response;


public enum ChessMatchWebsocketResponseEnum {
    INFO,
    CHESS_MOVE,
    CHESS_MOVE_BAD,
    DRAW,
    DRAW_BAD,
    REJECT_DRAW,
    REJECT_DRAW_BAD,
    ACCEPT_DRAW_BAD,
    SURRENDER_BAD,
    SUBSCRIBED,
    DISCONNECTED,
    MATCH_RESULT;


    public void checkBadResponseType() {
        if (this != CHESS_MOVE_BAD && this != DRAW_BAD && this != REJECT_DRAW_BAD && this != ACCEPT_DRAW_BAD
            && this != SURRENDER_BAD) {
            throw new RuntimeException("It's not a bad response type!");
        }
    }
}

