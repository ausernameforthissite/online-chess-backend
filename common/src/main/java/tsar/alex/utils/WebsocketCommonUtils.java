package tsar.alex.utils;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import tsar.alex.model.WebsocketSessionWrapper;

import java.util.concurrent.ScheduledFuture;

public class WebsocketCommonUtils {
    public static MessageHeaders prepareMessageHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor
                .create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

    public static void cancelOldTimeoutFinisher(WebsocketSessionWrapper websocketSessionWrapper,
                                                boolean mayInterruptWhenRunning) {
        ScheduledFuture<?> oldTimeoutFinisher = websocketSessionWrapper.getTimeoutFinisher();

        if (oldTimeoutFinisher != null) {
            oldTimeoutFinisher.cancel(mayInterruptWhenRunning);
        }
    }
}
