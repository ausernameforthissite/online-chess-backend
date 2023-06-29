package tsar.alex.websocket;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class WebsocketLogInterceptor implements ChannelInterceptor {

    private final WebsocketMessageLogger websocketMessageLogger;

    @Override
    public void afterSendCompletion(@NotNull Message<?> message, @NotNull MessageChannel channel, boolean sent, Exception ex) {
        websocketMessageLogger.logMessage(message);
    }
}
