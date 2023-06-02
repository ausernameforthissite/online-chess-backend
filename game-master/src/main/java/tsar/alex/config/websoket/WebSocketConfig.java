package tsar.alex.config.websoket;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import tsar.alex.config.websocket.CustomErrorHandler;
import tsar.alex.model.WebsocketSessionMap;
import tsar.alex.model.WebsocketSessionWrapper;
import tsar.alex.utils.Constants;
import tsar.alex.utils.TimeoutWebsocketCloseHandler;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ScheduledExecutorService scheduledExecutorService;
    private final WebsocketSessionMap websocketSessions;

    private TaskScheduler messageBrokerTaskScheduler;

    @Autowired
    @Lazy
    public void setMessageBrokerTaskScheduler(TaskScheduler messageBrokerTaskScheduler) {
        this.messageBrokerTaskScheduler = messageBrokerTaskScheduler;
    }


    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
            @Override
            public WebSocketHandler decorate(final WebSocketHandler handler) {
                return new WebSocketHandlerDecorator(handler) {

                    @Override
                    public void afterConnectionEstablished(final WebSocketSession session) throws Exception {

                        System.out.println("After connection");
                        System.out.println("Session id = " + session.getId());
                        ScheduledFuture<?> timeoutDisconnectTask = scheduledExecutorService.schedule(
                                                                new TimeoutWebsocketCloseHandler(session),
                                                                Constants.NOT_SUBSCRIBED_SESSION_TIMEOUT_MS,
                                                                TimeUnit.MILLISECONDS);
                        websocketSessions.put(session.getId(), new WebsocketSessionWrapper(session, timeoutDisconnectTask));

                        super.afterConnectionEstablished(session);
                    }

                };
            }
        });
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[] {5000, 5000})
                .setTaskScheduler(this.messageBrokerTaskScheduler);
        config.setApplicationDestinationPrefixes("/ws");

    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
        registry.setErrorHandler(new CustomErrorHandler());
    }
}
