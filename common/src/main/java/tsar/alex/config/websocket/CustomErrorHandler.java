package tsar.alex.config.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import tsar.alex.exception.WebsocketErrorCodeEnum;
import tsar.alex.exception.WebsocketException;

public class CustomErrorHandler extends StompSubProtocolErrorHandler {


    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        String preparedMessage;
        Throwable cause = ex.getCause();

        if (cause instanceof WebsocketException websocketException) {
            WebsocketErrorCodeEnum errorCode = websocketException.getCode();

            if (errorCode == null) {
                throw new RuntimeException("Websocket exception code can't be null!");
            } else if (errorCode == WebsocketErrorCodeEnum.RETRY_GENERAL) {
                preparedMessage = "Something is broken on the server. Please, try again.";
            } else {
                preparedMessage = websocketException.getMessage();
            }

            accessor.setNativeHeader("ErrorCode", errorCode.name());
        } else {
            preparedMessage = "Unknown server exception";
        }

        return MessageBuilder.createMessage(preparedMessage.getBytes(), accessor.getMessageHeaders());
    }
}
