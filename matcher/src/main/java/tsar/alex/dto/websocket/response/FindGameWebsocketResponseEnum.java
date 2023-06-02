package tsar.alex.dto.websocket.response;

public enum FindGameWebsocketResponseEnum {
    OK,
    CANCELED,
    GENERAL_BAD,
    FIND_GAME_BAD,
    CANCEL_BAD;

    public enum FindGameWebsocketBadResponseEnum {
        GENERAL_BAD,
        FIND_GAME_BAD,
        CANCEL_BAD;

        public FindGameWebsocketResponseEnum toFindGameWebsocketResponseEnum() {
            switch (this) {
                case GENERAL_BAD:
                    return FindGameWebsocketResponseEnum.GENERAL_BAD;
                case FIND_GAME_BAD:
                    return FindGameWebsocketResponseEnum.FIND_GAME_BAD;
                case CANCEL_BAD:
                    return FindGameWebsocketResponseEnum.CANCEL_BAD;
            }
            throw new RuntimeException("Incorrect type FindGameWebsocketBadResponseEnum value: " + this.name());
        }
    }
}
