package tsar.alex.exception;

public class FindMatchCloseConnectionException extends WebsocketCloseConnectionException {
    public FindMatchCloseConnectionException(String message) {
        super(message);
    }
}
