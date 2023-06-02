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

    public static void cancelOldTimeoutDisconnectTask(WebsocketSessionWrapper websocketSessionWrapper,
                                                boolean mayInterruptWhenRunning) {
        ScheduledFuture<?> oldTimeoutDisconnectTask = websocketSessionWrapper.getTimeoutDisconnectTask();

        if (oldTimeoutDisconnectTask != null) {
            oldTimeoutDisconnectTask.cancel(mayInterruptWhenRunning);
        }
    }
}