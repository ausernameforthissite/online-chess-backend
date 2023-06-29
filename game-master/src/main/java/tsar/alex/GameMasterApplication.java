package tsar.alex;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import tsar.alex.api.WebsocketMessageSender;
import tsar.alex.model.WebsocketSessionMap;

import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class GameMasterApplication {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;


    public static void main(String[] args) {
        SpringApplication.run(GameMasterApplication.class);
    }

    @Bean
    public ThreadLocalRandom threadLocalRandomBean() {
        return ThreadLocalRandom.current();
    }


    @Bean
    public WebsocketMessageSender websocketMessageSenderBean(ObjectMapper objectMapper,
            SimpMessagingTemplate simpMessagingTemplate) {
        return new WebsocketMessageSender(objectMapper, simpMessagingTemplate, "/queue/chess_game/response");
    }

    @Bean
    public JwtDecoder jwtDecoderBean() {
        return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
    }


    @Bean
    public WebsocketSessionMap webSocketSessionsBean() {
        return new WebsocketSessionMap();
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorServiceBean() {
        return Executors.newScheduledThreadPool(5);
    }
}
