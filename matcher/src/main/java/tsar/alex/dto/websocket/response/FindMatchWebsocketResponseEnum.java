package tsar.alex.dto.websocket.response;

public enum FindMatchWebsocketResponseEnum {
    OK,
    CANCELED,
    GENERAL_BAD,
    FIND_MATCH_BAD,
    CANCEL_BAD;

    public enum FindMatchWebsocketBadResponseEnum {
        GENERAL_BAD,
        FIND_MATCH_BAD,
        CANCEL_BAD;

        public FindMatchWebsocketResponseEnum toFindMatchWebsocketResponseEnum() {
            switch (this) {
                case GENERAL_BAD:
                    return FindMatchWebsocketResponseEnum.GENERAL_BAD;
                case FIND_MATCH_BAD:
                    return FindMatchWebsocketResponseEnum.FIND_MATCH_BAD;
                case CANCEL_BAD:
                    return FindMatchWebsocketResponseEnum.CANCEL_BAD;
            }
            throw new RuntimeException("Incorrect type FindMatchWebsocketBadResponseEnum value: " + this.name());
        }
    }
}
