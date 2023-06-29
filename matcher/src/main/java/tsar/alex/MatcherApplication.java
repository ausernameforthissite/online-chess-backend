package tsar.alex;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.web.client.RestTemplate;
import tsar.alex.api.WebsocketMessageSender;
import tsar.alex.model.WebsocketSessionMap;


@SpringBootApplication
@EnableScheduling
public class MatcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatcherApplication.class);
    }

    @Bean
    public RestTemplate restTemplateBean() {
        return new RestTemplate();
    }

    @Bean
    public WebsocketMessageSender websocketMessageSenderBean(ObjectMapper objectMapper,
            SimpMessagingTemplate simpMessagingTemplate) {
        return new WebsocketMessageSender(objectMapper, simpMessagingTemplate, "/queue/find_game/response");
    }

    @Bean
    public ThreadLocalRandom threadLocalRandomBean() {
        return ThreadLocalRandom.current();
    }


    @Bean
    public WebsocketSessionMap websocketSessionMapBean() {
        return new WebsocketSessionMap();
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorServiceBean() {
        return Executors.newSingleThreadScheduledExecutor();
    }

}
