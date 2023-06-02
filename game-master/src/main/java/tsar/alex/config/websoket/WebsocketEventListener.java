package tsar.alex.config.websoket;

import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import tsar.alex.model.WebsocketSessionMap;
import tsar.alex.model.WebsocketSessionWrapper;
import tsar.alex.utils.WebsocketCommonUtils;
import tsar.alex.api.websocket.ChessGameWebsocketRoom;
import tsar.alex.api.websocket.ChessGameWebsocketRoomsHolder;

@Component
@AllArgsConstructor
public class WebsocketEventListener {

    private final WebsocketSessionMap websocketSessions;
    private final ChessGameWebsocketRoomsHolder chessGameWebsocketRoomsHolder;

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

        String gameId = (String) authentication.getDetails();
        ChessGameWebsocketRoom gameWebsocketRoom = chessGameWebsocketRoomsHolder.getGameWebsocketRoom(gameId);

        if (gameWebsocketRoom == null) {
            return;
        }
        gameWebsocketRoom.reentrantLock.lock();

        try {
            String username = authentication.getName();
            gameWebsocketRoom.removeDisconnectedUser(username, sessionId);
        } finally {
            gameWebsocketRoom.reentrantLock.unlock();
        }

    }
}
