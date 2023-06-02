package tsar.alex.config.websoket;

import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import tsar.alex.model.WebsocketSessionWrapper;
import tsar.alex.model.WebsocketSessionMap;
import tsar.alex.utils.WebsocketCommonUtils;
import tsar.alex.api.websocket.UsersWaitingForGameWebsocketHolder;

@Component
@AllArgsConstructor
public class WebsocketEventListener {

    private final WebsocketSessionMap websocketSessions;
    private final UsersWaitingForGameWebsocketHolder uwfgWebsocketHolder;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        System.out.println("EventListener: Disconnected");
        String sessionId = event.getSessionId();
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);

        WebsocketSessionWrapper websocketSessionWrapper = websocketSessions.remove(sessionId);

        if (websocketSessionWrapper == null) {
            System.out.println("No session with id = " + event.getSessionId() + " was found");
            return;
        }

        WebsocketCommonUtils.cancelOldTimeoutDisconnectTask(websocketSessionWrapper, false);

        AbstractAuthenticationToken authentication = (AbstractAuthenticationToken) accessor.getUser();
        uwfgWebsocketHolder.removeDisconnectedUserIfPossible(authentication.getName(), sessionId);
    }
}
