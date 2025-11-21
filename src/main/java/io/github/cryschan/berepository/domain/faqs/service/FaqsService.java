package io.github.cryschan.berepository.domain.faqs.service;

import io.github.cryschan.berepository.domain.faqs.dto.response.FaqsResponse;
import io.github.cryschan.berepository.domain.faqs.repository.FaqsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class FaqsService {

    private final FaqsRepository faqsRepository;

    public List<FaqsResponse> getFaqs() {
        return faqsRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(FaqsResponse::from)
                .toList();
    }
}
