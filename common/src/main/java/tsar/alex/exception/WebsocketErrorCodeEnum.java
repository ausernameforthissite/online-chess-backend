package tsar.alex.exception;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum WebsocketErrorCodeEnum {
    RETRY_GENERAL,
    CLOSE_CONNECTION_GENERAL,
    CLOSE_CONNECTION_ALREADY_IN_MATCH,
    CLOSE_CONNECTION_NO_ACTIVE_MATCH,
    CLOSE_CONNECTION_ALREADY_SUBSCRIBED
}
