package tsar.alex.websocket;


import static tsar.alex.utils.CommonTextConstants.*;

import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;
import tsar.alex.api.client.MatcherRestClient;
import tsar.alex.exception.WebsocketErrorCodeEnum;
import tsar.alex.exception.WebsocketException;
import tsar.alex.model.ChessGameTypeWithTimings;
import tsar.alex.model.WebsocketSessionWrapper;


import java.util.List;
import tsar.alex.model.WebsocketSessionMap;


@Component
@RequiredArgsConstructor
public class WebsocketInboundInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final WebsocketSessionMap websocketSessions;
    private final MatcherRestClient matcherRestClient;

    private UsersWaitingForGameWebsocketHolder uwfgWebsocketHolder;

    @Autowired
    @Lazy
    public void setUwfgWebsocketHolder(UsersWaitingForGameWebsocketHolder uwfgWebsocketHolder) {
        this.uwfgWebsocketHolder = uwfgWebsocketHolder;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = Objects.requireNonNull(
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class));
        StompCommand command = Objects.requireNonNull(accessor.getCommand());

        switch (command) {
            case CONNECT -> handleConnect(accessor);
            case SUBSCRIBE -> handleSubscribe(accessor);
            case UNSUBSCRIBE -> handleUnsubscribe(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {

        if (!matcherRestClient.isGameMasterMicroserviceAvailable()) {
            throw new WebsocketException(SERVICE_UNAVAILABLE, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }

        List<String> authorization = accessor.getNativeHeader(X_AUTHORIZATION);
        AbstractAuthenticationToken authentication;

        if (authorization != null && authorization.size() == 1) {
            try {
                String accessToken = authorization.get(0).split(" ")[1];
                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                Jwt jwt = jwtDecoder.decode(accessToken);
                converter.setPrincipalClaimName("username");
                authentication = converter.convert(jwt);
                accessor.setUser(authentication);
            } catch (Exception e) {
                throw new WebsocketException(UNAUTHORIZED, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
            }
        } else {
            throw new WebsocketException(String.format(HEADER_NOT_PRESENT_OR_INCORRECT, X_AUTHORIZATION),
                    WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }

        List<String> searchGameTypeArray = accessor.getNativeHeader(GAME_TYPE);
        if (searchGameTypeArray != null && searchGameTypeArray.size() == 1) {
            try {
                String searchGameTypeString = searchGameTypeArray.get(0);
                ChessGameTypeWithTimings searchGameType = ChessGameTypeWithTimings.valueOf(
                        searchGameTypeString.toUpperCase());
                authentication.setDetails(searchGameType);
            } catch (Exception e) {
                throw new WebsocketException(INCORRECT_GAME_TYPE, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
            }
        } else {
            throw new WebsocketException(String.format(HEADER_NOT_PRESENT_OR_INCORRECT, GAME_TYPE),
                    WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        WebsocketSessionWrapper websocketSessionWrapper = websocketSessions.get(sessionId);

        if (websocketSessionWrapper == null) {
            throw new WebsocketException(String.format(SESSION_NOT_FOUND, sessionId),
                    WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }

        AbstractAuthenticationToken authentication = (AbstractAuthenticationToken) Objects.requireNonNull(
                accessor.getUser());
        ChessGameTypeWithTimings searchGameType = (ChessGameTypeWithTimings) authentication.getDetails();
        uwfgWebsocketHolder.addUserIfPossible(authentication.getName(), searchGameType, websocketSessionWrapper);
    }

    private void handleUnsubscribe(StompHeaderAccessor accessor) {
        String username = Objects.requireNonNull(accessor.getUser()).getName();
        String sessionId = accessor.getSessionId();

        throw new WebsocketException(String.format(CANNOT_UNSUBSCRIBE, username, sessionId),
                WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
    }

}
