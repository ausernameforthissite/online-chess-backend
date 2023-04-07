package tsar.alex.utils;

import lombok.AllArgsConstructor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@AllArgsConstructor
public class TimeoutWebsocketCloseHandler implements Runnable {

    private final WebSocketSession webSocketSession;

    @Override
    public void run() {
        try {
            if (webSocketSession.isOpen()) {
                webSocketSession.close(new CloseStatus(4000, "Timeouted!"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
