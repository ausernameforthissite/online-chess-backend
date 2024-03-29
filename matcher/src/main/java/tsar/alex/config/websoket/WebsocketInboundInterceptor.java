package tsar.alex.config.websoket;


import static tsar.alex.utils.CommonTextConstants.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;
import tsar.alex.exception.WebsocketErrorCodeEnum;
import tsar.alex.exception.WebsocketException;
import tsar.alex.model.WebsocketSessionWrapper;
import tsar.alex.utils.websocket.UsersWaitingForMatchWebsocketHolder;


import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class WebsocketInboundInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final Map<String, WebsocketSessionWrapper> websocketSessions;

    private UsersWaitingForMatchWebsocketHolder UWFMWebsocketHolder;

    @Autowired
    @Lazy
    public void setUWFMWebsocketHolder(UsersWaitingForMatchWebsocketHolder UWFMWebsocketHolder) {
        this.UWFMWebsocketHolder = UWFMWebsocketHolder;
    }

    @Override
    public Message<?> preSend(Message message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();

        System.out.println("Websocket sessions: " + websocketSessions);

        if (StompCommand.CONNECT.equals(command)) {

            System.out.println("Connecting...");
            List<String> authorization = accessor.getNativeHeader("X-Authorization");

            if (authorization != null && authorization.size() == 1) {
                try {
                    String accessToken = authorization.get(0).split(" ")[1];
                    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                    Jwt jwt = jwtDecoder.decode(accessToken);
                    converter.setPrincipalClaimName("username");
                    Authentication authentication = converter.convert(jwt);
                    accessor.setUser(authentication);
                } catch (Exception e) {
                    throw new WebsocketException(UNAUTHORIZED, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
                }
            } else {
                throw new WebsocketException(NO_AUTHORIZATION_HEADER, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
            }
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            System.out.println("Subscribing...");
            String sessionId = accessor.getSessionId();
            WebsocketSessionWrapper websocketSessionWrapper = websocketSessions.get(sessionId);

            if (websocketSessionWrapper == null) {
                throw new WebsocketException(String.format(NO_ACTIVE_SESSION, sessionId), WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
            }

            String username = accessor.getUser().getName();
            UWFMWebsocketHolder.addUserIfPossible(username, websocketSessionWrapper);
        }

        if (StompCommand.UNSUBSCRIBE.equals(command)) {
            System.out.println("Unsubscribing...");
            String username = accessor.getUser().getName();
            String sessionId = accessor.getSessionId();

            throw new WebsocketException(String.format(CANNOT_UNSUBSCRIBE, username, sessionId), WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }

        if (StompCommand.DISCONNECT.equals(command)) {
            System.out.println("Disconnecting...");
        }

        return message;
    }

}
