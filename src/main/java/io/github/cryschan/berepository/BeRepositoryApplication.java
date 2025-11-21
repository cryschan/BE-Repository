package io.github.cryschan.berepository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BeRepositoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeRepositoryApplication.class, args);
    }

}
