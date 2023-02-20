package tsar.alex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import tsar.alex.utils.sse.ChessMoveSseEmitters;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class GameMasterApplication {
    public static void main(String[] args) {
        SpringApplication.run(GameMasterApplication.class);
    }

    @Bean
    public ThreadLocalRandom threadLocalRandomBean() {
        return ThreadLocalRandom.current();
    }

    @Bean
    public Map<Long, ChessMoveSseEmitters> emittersBean() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public ExecutorService threadPoolBean() {
        return Executors.newCachedThreadPool();
    }
}
