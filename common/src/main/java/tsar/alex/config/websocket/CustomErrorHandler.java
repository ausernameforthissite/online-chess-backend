package tsar.alex.config.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import tsar.alex.exception.WebsocketCloseConnectionException;

public class CustomErrorHandler extends StompSubProtocolErrorHandler {


    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);

        String preparedMessage;
        Throwable cause = ex.getCause();

        if (cause instanceof WebsocketCloseConnectionException) {
            preparedMessage = cause.getMessage();
            accessor.setNativeHeader("Close-Connection", "true");
        } else {
            preparedMessage = "Unknown server exception";
        }

        return MessageBuilder.createMessage(preparedMessage.getBytes(), accessor.getMessageHeaders());
    }
}
