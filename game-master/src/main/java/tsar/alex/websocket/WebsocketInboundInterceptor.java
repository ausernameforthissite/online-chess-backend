package tsar.alex.websocket;

import static tsar.alex.utils.CommonTextConstants.*;
import static tsar.alex.utils.Utils.validateGameId;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
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
import tsar.alex.exception.WebsocketErrorCodeEnum;
import tsar.alex.exception.WebsocketException;
import tsar.alex.model.WebsocketSessionMap;
import tsar.alex.model.WebsocketSessionWrapper;
import tsar.alex.utils.WebsocketCommonUtils;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebsocketInboundInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final WebsocketSessionMap websocketSessions;

    private ChessGameWebsocketRoomsHolder chessGameWebsocketRoomsHolder;

    @Autowired
    @Lazy
    public void setChessGameWebsocketRoomsHolder(ChessGameWebsocketRoomsHolder chessGameWebsocketRoomsHolder) {
        this.chessGameWebsocketRoomsHolder = chessGameWebsocketRoomsHolder;
    }


    @Override
    public Message<?> preSend(@NotNull Message message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            System.out.println("Connect");
            List<String> gameIdHeader = accessor.getNativeHeader("game-Id");

            String gameId;

            if (gameIdHeader != null && gameIdHeader.size() == 1) {
                gameId = gameIdHeader.get(0);

                String errorMessage = validateGameId(gameId);
                if (errorMessage != null) {
                    throw new WebsocketException(errorMessage, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
                }
            } else {
                throw new WebsocketException(NO_GAME_ID_HEADER, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
            }

            System.out.println("Game id: " + gameId);

            ChessGameWebsocketRoom gameWebsocketRoom = chessGameWebsocketRoomsHolder.getGameWebsocketRoom(gameId);

            if (gameWebsocketRoom == null) {
                throw new WebsocketException(String.format(NO_ACTIVE_GAME, gameId),
                        WebsocketErrorCodeEnum.CLOSE_CONNECTION_NO_ACTIVE_GAME);
            }

            List<String> authorization = accessor.getNativeHeader("X-Authorization");
            JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
            Jwt jwt;

            if (authorization != null && authorization.size() == 1) {
                String accessToken = authorization.get(0).split(" ")[1];

                try {
                    jwt = jwtDecoder.decode(accessToken);
                } catch (Exception e) {
                    throw new WebsocketException(UNAUTHORIZED, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
                }
            } else {
                jwt = Jwt.withTokenValue("0").header("typ", "JWT").claim("username",
                        UUID.randomUUID().toString()).claim("scope", "ROLE_USER").build();
            }

            converter.setPrincipalClaimName("username");
            AbstractAuthenticationToken authentication = converter.convert(jwt);
            authentication.setDetails(gameId);
            accessor.setUser(authentication);
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            System.out.println("Subscribing...");
            String sessionId = accessor.getSessionId();
            WebsocketSessionWrapper websocketSessionWrapper = websocketSessions.get(sessionId);

            if (websocketSessionWrapper == null) {
                throw new WebsocketException(String.format(SESSION_NOT_FOUND, sessionId),
                        WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
            }

            WebsocketCommonUtils.cancelOldTimeoutDisconnectTask(websocketSessionWrapper);
            AbstractAuthenticationToken authentication = (AbstractAuthenticationToken) accessor.getUser();
            String gameId = (String) authentication.getDetails();
            ChessGameWebsocketRoom GameWebsocketRoom = chessGameWebsocketRoomsHolder.getGameWebsocketRoom(gameId);

            if (GameWebsocketRoom == null) {
                throw new WebsocketException(String.format(NO_ACTIVE_GAME, gameId),
                        WebsocketErrorCodeEnum.CLOSE_CONNECTION_NO_ACTIVE_GAME);
            }

            GameWebsocketRoom.reentrantLock.lock();

            try {
                GameWebsocketRoom.checkFinished();
                String username = authentication.getName();
                GameWebsocketRoom.addSubscribedUserIfPossible(username, websocketSessionWrapper);
            } finally {
                GameWebsocketRoom.reentrantLock.unlock();
            }

        }

        if (StompCommand.UNSUBSCRIBE.equals(command)) {
            System.out.println("Unsubscribe");
            String username = accessor.getUser().getName();
            String sessionId = accessor.getSessionId();

            throw new WebsocketException(String.format(CANNOT_UNSUBSCRIBE, username, sessionId),
                    WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }

        if (StompCommand.DISCONNECT.equals(command)) {
            System.out.println("Disconnecting...");
        }

        return message;
    }

}
