package tsar.alex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.context.annotation.ApplicationScope;
import tsar.alex.model.UserWaitingForMatch;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


@SpringBootApplication
@EnableScheduling
public class MatcherApplication {
    public static void main(String[] args) {
        Arrays.stream(args).forEach(System.out::println);
        SpringApplication.run(MatcherApplication.class);
    }

    @Bean
    @ApplicationScope
    public Set<UserWaitingForMatch> usersWaitingForMatchBean() {
        return new HashSet<>();
    }

    @Bean
    public ThreadLocalRandom threadLocalRandomBean() {
        return ThreadLocalRandom.current();
    }

}
