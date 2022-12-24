package tsar.alex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
public class GameMasterApplication {
    public static void main(String[] args) {
        SpringApplication.run(GameMasterApplication.class);
    }

    @Bean
    public ThreadLocalRandom threadLocalRandomBean() {
        return ThreadLocalRandom.current();
    }
}
