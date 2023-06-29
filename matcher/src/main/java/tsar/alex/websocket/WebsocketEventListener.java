package tsar.alex.websocket;

import static tsar.alex.utils.CommonTextConstants.*;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import tsar.alex.model.WebsocketSessionWrapper;
import tsar.alex.model.WebsocketSessionMap;
import tsar.alex.utils.WebsocketCommonUtils;

@Component
@AllArgsConstructor
@Slf4j
public class WebsocketEventListener {

    private final WebsocketSessionMap websocketSessions;
    private final UsersWaitingForGameWebsocketHolder uwfgWebsocketHolder;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.debug(String.format(WEBSOCKET_DISCONNECT_LOG, sessionId));

        StompHeaderAccessor accessor = Objects.requireNonNull(
                MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class));

        WebsocketSessionWrapper websocketSessionWrapper = websocketSessions.remove(sessionId);

        if (websocketSessionWrapper == null) {
            log.debug(String.format(SESSION_NOT_FOUND, sessionId));
            return;
        }

        WebsocketCommonUtils.cancelOldTimeoutDisconnectTask(websocketSessionWrapper);

        AbstractAuthenticationToken authentication = (AbstractAuthenticationToken) accessor.getUser();

        if (authentication != null) {
            uwfgWebsocketHolder.removeDisconnectedUserIfPossible(authentication.getName(), sessionId);
        }
    }
}
