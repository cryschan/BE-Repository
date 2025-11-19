package io.github.cryschan.berepository.domain.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AiService {

    private final ChatClient chatClient;

    public String comment(String message) {
        return chatClient
                .prompt()
                .user(message)
                .call()
                .content();
    }
}
