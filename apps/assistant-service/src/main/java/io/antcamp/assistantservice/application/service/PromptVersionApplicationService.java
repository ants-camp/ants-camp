package io.antcamp.assistantservice.application.service;

import io.antcamp.assistantservice.domain.model.PromptVersion;
import io.antcamp.assistantservice.domain.repository.PromptVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptVersionApplicationService {

    private final PromptVersionRepository promptVersionRepository;

    public PromptVersion save(String name, String content) {
        return promptVersionRepository.save(PromptVersion.create(name, content));
    }

    public List<PromptVersion> findAll() {
        return promptVersionRepository.findAll();
    }
}