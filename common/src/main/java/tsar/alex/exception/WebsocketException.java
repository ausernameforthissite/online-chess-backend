package tsar.alex.exception;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebsocketException extends RuntimeException {
    private WebsocketErrorCodeEnum code;

    public WebsocketException(String message, WebsocketErrorCodeEnum code) {
        super(message);
        this.code = code;
    }

    public WebsocketException(Throwable cause, WebsocketErrorCodeEnum code) {
        super(cause);
        this.code = code;
    }
}
