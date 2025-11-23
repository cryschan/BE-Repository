package io.github.cryschan.berepository._global.config.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }
}
