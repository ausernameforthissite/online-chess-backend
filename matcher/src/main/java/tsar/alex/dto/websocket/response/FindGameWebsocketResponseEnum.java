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
            return switch (this) {
                case GENERAL_BAD -> FindGameWebsocketResponseEnum.GENERAL_BAD;
                case FIND_GAME_BAD -> FindGameWebsocketResponseEnum.FIND_GAME_BAD;
                case CANCEL_BAD -> FindGameWebsocketResponseEnum.CANCEL_BAD;
            };

        }
    }
}
