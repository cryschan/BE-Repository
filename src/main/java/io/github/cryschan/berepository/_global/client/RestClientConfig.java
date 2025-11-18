package io.github.cryschan.berepository._global.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

// RestClient 전역 클래스
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("https://content.musinsa.com")
                .defaultHeader("content-Type", "application/json")
                .build();
    }
}
