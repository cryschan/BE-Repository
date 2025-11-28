package io.github.cryschan.berepository.domain.ai.config;

import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 로깅 설정
 * Spring AI의 SimpleLoggerAdvisor를 사용하여 모든 AI 호출에 대한 로깅을 수행합니다.
 */
@Configuration
public class AiLoggingConfig {

    @Bean
    public SimpleLoggerAdvisor simpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }
}
