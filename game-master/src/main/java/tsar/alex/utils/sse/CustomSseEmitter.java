package tsar.alex.utils.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class CustomSseEmitter extends SseEmitter {

    public CustomSseEmitter() {
    }

    public CustomSseEmitter(Long timeout) {
        super(timeout);
    }

    public void sendErrorAndComplete(String message) {
        try {
            send("{\"error\": \"" + message + "\"}");
            completeWithError(new Throwable(message));
        } catch (Exception e) {
            completeWithError(e);
        }
    }

    public void sendOkResponse() {
        try {
            send("{\"type\": \"ok\"}");
        } catch (Exception e) {
            completeWithError(e);
        }
    }
}
