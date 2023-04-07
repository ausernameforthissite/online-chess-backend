package tsar.alex;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import tsar.alex.model.WebsocketSessionWrapper;

import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;


@SpringBootApplication
@EnableScheduling
public class MatcherApplication {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;


    public static void main(String[] args) {
        Arrays.stream(args).forEach(System.out::println);
        SpringApplication.run(MatcherApplication.class);
    }


    @Bean
    public ThreadLocalRandom threadLocalRandomBean() {
        return ThreadLocalRandom.current();
    }


    @Bean
    public JwtDecoder jwtDecoderBean() {
        return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
    }


    @Bean
    public Map<String, WebsocketSessionWrapper> webSocketSessionsBean() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorServiceBean() {
        return Executors.newSingleThreadScheduledExecutor();
    }

}
