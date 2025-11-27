package io.github.cryschan.berepository._global.config.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

// RestClient 전역 클래스
@Configuration
public class RestClientConfig {

    @Bean
    @Qualifier("musinsaContentRestClient")
    public RestClient musinsaContentRestClient() {
        return RestClient.builder()
                .baseUrl("https://content.musinsa.com")
                .defaultHeader("content-Type", "application/json")
                .build();
    }

    @Bean
    @Qualifier("musinsaRankingRestClient")
    public RestClient musinsaRankingRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.musinsa.com")
                .defaultHeader("content-Type", "application/json")
                .build();
    }
}
