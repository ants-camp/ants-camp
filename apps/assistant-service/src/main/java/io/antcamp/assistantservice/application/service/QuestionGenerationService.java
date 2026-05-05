package io.antcamp.assistantservice.application.service;

import io.antcamp.assistantservice.application.port.LlmPort;
import io.antcamp.assistantservice.domain.model.DocumentChunk;
import io.antcamp.assistantservice.domain.model.EvalQuestion;
import io.antcamp.assistantservice.domain.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionGenerationService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final LlmPort llmPort;

    public List<EvalQuestion> generateQuestions(int count) {
        // 인제스트 완료된 청크에서 count개 샘플링
        List<DocumentChunk> chunks = knowledgeDocumentRepository.findRandomChunks(count);
        if (chunks.isEmpty()) {
            log.warn("질문 자동 생성 실패: 인제스트 완료된 청크가 없습니다.");
            return List.of();
        }

        List<EvalQuestion> questions = new ArrayList<>();
        for (DocumentChunk chunk : chunks) {
            try {
                // 청크 내용을 정답(referenceAnswer)으로 활용 → Context Recall 산출의 ground truth
                String question = llmPort.generateQuestion(chunk.getContent());
                questions.add(new EvalQuestion(question, chunk.getContent()));
                log.debug("질문 생성 완료: chunkId={}", chunk.getDocumentChunkId());
            } catch (Exception e) {
                log.warn("청크에서 질문 생성 실패, 건너뜀: chunkId={}", chunk.getDocumentChunkId(), e);
            }
        }
        return questions;
    }
}