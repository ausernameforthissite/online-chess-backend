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
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;
import tsar.alex.exception.WebsocketErrorCodeEnum;
import tsar.alex.exception.WebsocketException;
import tsar.alex.model.WebsocketSessionWrapper;
import tsar.alex.utils.WebsocketCommonUtils;
import tsar.alex.utils.websocket.ChessMatchWebsocketRoom;
import tsar.alex.utils.websocket.ChessMatchWebsocketRoomsHolder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebsocketInboundInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final Map<String, WebsocketSessionWrapper> websocketSessions;

    private ChessMatchWebsocketRoomsHolder chessMatchWebsocketRoomsHolder;

    @Autowired
    @Lazy
    public void setChessMatchWebsocketRoomsHolder(ChessMatchWebsocketRoomsHolder chessMatchWebsocketRoomsHolder) {
        this.chessMatchWebsocketRoomsHolder = chessMatchWebsocketRoomsHolder;
    }


    @Override
    public Message<?> preSend(Message message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            System.out.println("Connect");
            List<String> matchIdHeader = accessor.getNativeHeader("Match-Id");

            long matchId;

            if (matchIdHeader != null && matchIdHeader.size() == 1) {
                try {
                    matchId = Long.parseLong(matchIdHeader.get(0));
                } catch (NumberFormatException e) {
                    throw new WebsocketException(INCORRECT_MATCH_ID, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
                }

            } else {
                throw new WebsocketException(NO_MATCH_ID_HEADER, WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
            }

            System.out.println("Match id: " + matchId);


            ChessMatchWebsocketRoom matchWebsocketRoom = chessMatchWebsocketRoomsHolder.getMatchWebsocketRoom(matchId);

            if (matchWebsocketRoom == null) {
                throw new WebsocketException(String.format(NO_ACTIVE_MATCH, matchId), WebsocketErrorCodeEnum.CLOSE_CONNECTION_NO_ACTIVE_MATCH);
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
            authentication.setDetails(matchId);
            accessor.setUser(authentication);
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            System.out.println("Subscribing...");
            String sessionId = accessor.getSessionId();
            WebsocketSessionWrapper websocketSessionWrapper = websocketSessions.get(sessionId);

            if (websocketSessionWrapper == null) {
                throw new WebsocketException(String.format(NO_ACTIVE_SESSION, sessionId), WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
            }

            WebsocketCommonUtils.cancelOldTimeoutFinisher(websocketSessionWrapper, true);
            AbstractAuthenticationToken authentication = (AbstractAuthenticationToken) accessor.getUser();
            long matchId = (Long) authentication.getDetails();
            ChessMatchWebsocketRoom matchWebsocketRoom = chessMatchWebsocketRoomsHolder.getMatchWebsocketRoom(matchId);

            if (matchWebsocketRoom == null) {
                throw new WebsocketException(String.format(NO_ACTIVE_MATCH, matchId), WebsocketErrorCodeEnum.CLOSE_CONNECTION_NO_ACTIVE_MATCH);
            }

            matchWebsocketRoom.reentrantLock.lock();

            try {
                matchWebsocketRoom.checkFinished();
                String username = authentication.getName();
                matchWebsocketRoom.addSubscribedUserIfPossible(username, websocketSessionWrapper);
            } finally {
                matchWebsocketRoom.reentrantLock.unlock();
            }

        }

        if (StompCommand.UNSUBSCRIBE.equals(command)) {
            System.out.println("Unsubscribe");
            String username = ((AbstractAuthenticationToken) accessor.getUser()).getName();
            String sessionId = accessor.getSessionId();

            throw new WebsocketException(String.format(CANNOT_UNSUBSCRIBE, username, sessionId), WebsocketErrorCodeEnum.CLOSE_CONNECTION_GENERAL);
        }

        if (StompCommand.DISCONNECT.equals(command)) {
            System.out.println("Disconnecting...");
        }


        return message;
    }

}
