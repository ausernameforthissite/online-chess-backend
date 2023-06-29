package tsar.alex.websocket;

import static tsar.alex.utils.CommonTextConstants.ERROR_CODE;
import static tsar.alex.utils.CommonTextConstants.NO_EXCEPTION_CODE;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import tsar.alex.exception.WebsocketErrorCodeEnum;
import tsar.alex.exception.WebsocketException;

@Component
@AllArgsConstructor
@Slf4j
public class WebsocketErrorHandler extends StompSubProtocolErrorHandler {

    private final WebsocketMessageLogger websocketMessageLogger;

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        String preparedMessage;
        Throwable cause = ex.getCause();
        log.warn(cause.toString());

        if (cause instanceof WebsocketException websocketException) {
            WebsocketErrorCodeEnum errorCode = websocketException.getCode();

            if (errorCode == null) {
                log.error(NO_EXCEPTION_CODE);
                throw new RuntimeException(NO_EXCEPTION_CODE);
            } else if (errorCode == WebsocketErrorCodeEnum.RETRY_GENERAL) {
                preparedMessage = "Something is broken on the server. Please, try again.";
            } else {
                preparedMessage = websocketException.getMessage();
            }

            accessor.setNativeHeader(ERROR_CODE, errorCode.name());
        } else {
            preparedMessage = "Unknown server exception.";
        }

        Message<byte[]> message = MessageBuilder.createMessage(preparedMessage.getBytes(),
                accessor.getMessageHeaders());
        websocketMessageLogger.logMessage(message);
        return message;
    }
}
