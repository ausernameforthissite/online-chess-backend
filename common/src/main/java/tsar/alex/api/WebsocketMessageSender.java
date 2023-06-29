package tsar.alex.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.WebSocketSession;
import tsar.alex.dto.WebsocketResponse;
import tsar.alex.utils.WebsocketCommonUtils;


@AllArgsConstructor
@Slf4j
public class WebsocketMessageSender {
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final String destination;

    public void sendMessageToWebsocketSession(WebsocketResponse response, WebSocketSession websocketSession) {
        sendMessageToWebsocketSession(mapResponseToJsonString(response), websocketSession);
    }

    public void sendMessageToWebsocketSession(String responseAsJsonString, WebSocketSession websocketSession) {
        String sessionId = websocketSession.getId();
        MessageHeaders headers = WebsocketCommonUtils.prepareMessageHeaders(sessionId);

        if (websocketSession.isOpen()) {
            try {
                messagingTemplate.convertAndSendToUser(sessionId, destination, responseAsJsonString, headers);
            } catch (MessagingException e) {
                log.error(e.toString());
                e.printStackTrace();
            }
        }
    }

    public String mapResponseToJsonString(WebsocketResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}