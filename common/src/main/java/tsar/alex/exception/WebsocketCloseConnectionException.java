package tsar.alex.exception;

public class WebsocketCloseConnectionException extends WebsocketException {
    public WebsocketCloseConnectionException() {
    }

    public WebsocketCloseConnectionException(String message) {
        super(message);
    }
}
