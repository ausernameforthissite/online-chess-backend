package tsar.alex.exception;


public class WebsocketException extends RuntimeException {
    public WebsocketException() {
    }

    public WebsocketException(String message) {
        super(message);
    }
}
