package tsar.alex.exception;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum WebsocketErrorCodeEnum {
    RETRY_GENERAL,
    CLOSE_CONNECTION_GENERAL,
    CLOSE_CONNECTION_ALREADY_IN_GAME,
    CLOSE_CONNECTION_NO_ACTIVE_GAME,
    CLOSE_CONNECTION_ALREADY_SUBSCRIBED
}