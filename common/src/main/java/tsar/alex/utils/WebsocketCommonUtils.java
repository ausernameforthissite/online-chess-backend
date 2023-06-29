package tsar.alex.utils;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import tsar.alex.model.WebsocketSessionWrapper;

import java.util.concurrent.ScheduledFuture;

public class WebsocketCommonUtils {

    public static MessageHeaders prepareMessageHeaders(String sessionId) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

    public static void cancelOldTimeoutDisconnectTask(WebsocketSessionWrapper websocketSessionWrapper) {
        cancelOldTimeoutDisconnectTask(websocketSessionWrapper, true);
    }

    public static void cancelOldTimeoutDisconnectTask(WebsocketSessionWrapper websocketSessionWrapper,
            boolean mayInterruptIfRunning) {
        ScheduledFuture<?> oldTimeoutDisconnectTask = websocketSessionWrapper.getTimeoutDisconnectTask();

        if (oldTimeoutDisconnectTask != null) {
            oldTimeoutDisconnectTask.cancel(mayInterruptIfRunning);
        }
    }

}