package tsar.alex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class MatcherApplication {
    public static void main(String[] args) {
        SpringApplication.run(MatcherApplication.class);
    }
}
