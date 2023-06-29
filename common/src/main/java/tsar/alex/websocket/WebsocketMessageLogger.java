package tsar.alex.websocket;

import static tsar.alex.utils.CommonTextConstants.*;

import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebsocketMessageLogger {

    private static final String STOMP_COMMAND = "stompCommand";
    private static final String SIMP_MESSAGE_TYPE = "simpMessageType";
    private static final String SIMP_SESSION_ID = "simpSessionId";
    private static final String NATIVE_HEADERS = "nativeHeaders";
    private static final String SIMP_DESTINATION = "simpDestination";
    private static final String HEART_BEAT = "heart-beat";

    public void logMessage(@NonNull Message<?> message) {
        MessageHeaders headers = message.getHeaders();

        StringBuilder stringBuilder = new StringBuilder("{type=");

        Object stompCommand = headers.get(STOMP_COMMAND);

        if (stompCommand != null) {
            stringBuilder.append("websocket_request");
            stringBuilder.append(", ").append(STOMP_COMMAND).append("=").append(stompCommand);
        } else {
            stringBuilder.append("websocket_response");
            stringBuilder.append(", ").append(STOMP_COMMAND).append("=").append(headers.get(SIMP_MESSAGE_TYPE));
        }

        appendHeaderIfPresent(stringBuilder, headers, SIMP_SESSION_ID);
        appendHeaderIfPresent(stringBuilder, headers, SIMP_DESTINATION);

        Map<String, Object> nativeHeaders = (Map<String, Object>) headers.get(NATIVE_HEADERS);

        if (nativeHeaders != null) {
            MessageHeaders nativeMessageHeaders = new MessageHeaders(nativeHeaders);
            appendHeaderIfPresent(stringBuilder, nativeMessageHeaders, ERROR_CODE);
            appendHeaderIfPresent(stringBuilder, nativeMessageHeaders, X_AUTHORIZATION);
            appendHeaderIfPresent(stringBuilder, nativeMessageHeaders, GAME_TYPE);
            appendHeaderIfPresent(stringBuilder, nativeMessageHeaders, HEART_BEAT);
        }

        String body = new String((byte[]) message.getPayload());

        if (!body.isEmpty()) {
            stringBuilder.append(", ").append("body=").append(body);
        }

        stringBuilder.append("}");
        log.trace(stringBuilder.toString());
    }

    private void appendHeaderIfPresent(StringBuilder stringBuilder, MessageHeaders headers, String name) {
        Object value = headers.get(name);

        if (value != null) {
            String stringValue = value.toString();
            if (name.equals(X_AUTHORIZATION)) {
                stringValue = stringValue.replaceFirst(" [\\w.-]+]", " ***this token is hidden by security policy***]");
            }
            stringBuilder.append(", ").append(name).append("=").append(stringValue);
        }
    }

}