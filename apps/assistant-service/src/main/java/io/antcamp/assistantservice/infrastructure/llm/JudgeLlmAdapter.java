package io.antcamp.assistantservice.infrastructure.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.antcamp.assistantservice.application.port.JudgeLlmPort;
import io.antcamp.assistantservice.domain.model.EvalScores;
import io.antcamp.assistantservice.domain.model.Verdict;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class JudgeLlmAdapter implements JudgeLlmPort {

    // Reference-free: 정답 없이 컨텍스트 기반으로만 채점
    private static final String JUDGE_FREE_PROMPT = """
            당신은 RAG(Retrieval-Augmented Generation) 품질 평가 전문가입니다.
            아래 기준으로 AI 어시스턴트의 응답을 1.0 ~ 5.0 점 척도로 평가하세요.

            - relevance(관련성): 응답이 질문의 의도에 정확히 부합하는가?
            - faithfulness(충실도): 응답이 제공된 컨텍스트에만 근거하는가? (환각 방지)
              컨텍스트에 없는 내용이 포함되어 있으면 3.0 미만으로 평가.
            - contextPrecision(맥락 정확도): 검색된 컨텍스트가 응답에 유용하게 활용되었는가?

            점수 가이드: 5.0=완벽  4.0=양호  3.0=보통  2.0=불량  1.0=완전히 틀림

            마크다운 없이 아래 JSON만 반환하세요:
            {"relevance":<1.0-5.0>,"faithfulness":<1.0-5.0>,"contextPrecision":<1.0-5.0>,"feedback":"<간략한 한국어 코멘트>"}
            """;

    // Reference-based: 정답과 비교하여 일치도 포함 채점
    private static final String JUDGE_REF_PROMPT = """
            당신은 RAG(Retrieval-Augmented Generation) 품질 평가 전문가입니다.
            제공된 정답을 기준으로 AI 어시스턴트의 응답을 1.0 ~ 5.0 점 척도로 평가하세요.

            - relevance(관련성): 응답이 질문의 의도에 정확히 부합하는가?
            - faithfulness(충실도): 응답이 정답 및 컨텍스트에 근거하는가? (정답과 상충하면 3.0 미만)
            - contextPrecision(맥락 정확도): 검색된 컨텍스트가 응답에 유용하게 활용되었는가?

            점수 가이드: 5.0=완벽  4.0=양호  3.0=보통  2.0=불량  1.0=완전히 틀림

            마크다운 없이 아래 JSON만 반환하세요:
            {"relevance":<1.0-5.0>,"faithfulness":<1.0-5.0>,"contextPrecision":<1.0-5.0>,"feedback":"<간략한 한국어 코멘트>"}
            """;

    // Pairwise: 두 응답을 비교하여 어느 쪽이 더 나은지 판정
    private static final String JUDGE_PAIRWISE_PROMPT = """
            당신은 RAG 응답 품질 비교 전문가입니다.
            아래 두 AI 응답 중 어느 쪽이 질문에 더 정확하고 충실하게 답변
            
            
            했는지 판정하세요.

            판정 기준: 관련성, 사실적 정확성, 컨텍스트 활용도
            결과: "A", "B", "TIE" 중 하나만 반환하세요. 다른 텍스트는 절대 포함하지 마세요.
            """;

    private final Map<Provider, ChatModel> modelRegistry;
    private final ObjectMapper objectMapper;

    public JudgeLlmAdapter(OpenAiChatModel openAiModel,
                           @Autowired(required = false) AnthropicChatModel anthropicModel,
                           @Autowired(required = false) VertexAiGeminiChatModel geminiModel,
                           ObjectMapper objectMapper) {
        Map<Provider, ChatModel> registry = new EnumMap<>(Provider.class);
        registry.put(Provider.OPENAI, openAiModel);
        if (anthropicModel != null) registry.put(Provider.ANTHROPIC, anthropicModel);
        if (geminiModel != null) registry.put(Provider.GEMINI, geminiModel);
        this.modelRegistry = Collections.unmodifiableMap(registry);
        this.objectMapper = objectMapper;
    }

    @Override
    public JudgeLlmPort.JudgeEvalResult evaluate(String judgeModel, String question, String llmResponse,
                                                   String retrievedContext, String referenceAnswer) {
        boolean hasRef = referenceAnswer != null && !referenceAnswer.isBlank();
        String systemPrompt = hasRef ? JUDGE_REF_PROMPT : JUDGE_FREE_PROMPT;
        String userContent = hasRef
                ? """
                [질문]
                %s

                [정답]
                %s

                [검색된 컨텍스트]
                %s

                [AI 응답]
                %s
                """.formatted(esc(question), esc(referenceAnswer), esc(retrievedContext), esc(llmResponse))
                : """
                [질문]
                %s

                [검색된 컨텍스트]
                %s

                [AI 응답]
                %s
                """.formatted(esc(question), esc(retrievedContext), esc(llmResponse));

        var resolved = resolveModelAndPrompt(judgeModel, systemPrompt, userContent);
        long start = System.currentTimeMillis();
        ChatResponse response = resolved.model().call(resolved.prompt());
        int latencyMs = (int) (System.currentTimeMillis() - start);

        String json = response.getResult().getOutput().getText().trim();
        EvalScores scores = parseScores(judgeModel, json);

        Usage usage = response.getMetadata().getUsage();
        int promptTokens = (usage != null && usage.getPromptTokens() != null) ? usage.getPromptTokens() : 0;
        int completionTokens = (usage != null && usage.getCompletionTokens() != null) ? usage.getCompletionTokens() : 0;

        return new JudgeLlmPort.JudgeEvalResult(scores, latencyMs, promptTokens, completionTokens);
    }

    @Override
    public Verdict compare(String judgeModel, String question, String responseA, String responseB) {
        String userContent = """
                [질문]
                %s

                [응답 A]
                %s

                [응답 B]
                %s
                """.formatted(esc(question), esc(responseA), esc(responseB));

        var resolved = resolveModelAndPrompt(judgeModel, JUDGE_PAIRWISE_PROMPT, userContent);
        ChatResponse response = resolved.model().call(resolved.prompt());
        String text = Optional.ofNullable(response.getResult())
                .map(r -> r.getOutput().getText())
                .orElseThrow(() -> new IllegalStateException(
                        "Pairwise Judge LLM 응답 없음: judgeModel=" + judgeModel));
        return switch (text.trim().toUpperCase()) {
            case "A" -> Verdict.A_WINS;
            case "B" -> Verdict.B_WINS;
            default  -> Verdict.TIE;
        };
    }

    private record ModelWithPrompt(ChatModel model, Prompt prompt) {}

    private ModelWithPrompt resolveModelAndPrompt(String judgeModel, String systemPrompt, String userContent) {
        List<Message> messages = List.of(new SystemMessage(systemPrompt), new UserMessage(userContent));
        Provider provider = Provider.from(judgeModel);
        ChatModel model = modelRegistry.get(provider);
        if (model == null) {
            throw new IllegalArgumentException(
                    "등록되지 않은 Judge provider — 빈 미등록 또는 잘못된 모델명: judgeModel=" + judgeModel);
        }
        Prompt prompt = buildPrompt(provider, judgeModel, messages);
        return new ModelWithPrompt(model, prompt);
    }

    // 외부 입력의 % 문자가 format specifier로 해석되는 것을 방지
    private static String esc(String s) {
        return s == null ? "" : s.replace("%", "%%");
    }

    // Judge는 결정성이 생명 — temperature 0.0 고정
    private Prompt buildPrompt(Provider provider, String judgeModel, List<Message> messages) {
        return switch (provider) {
            case ANTHROPIC -> new Prompt(messages,
                    AnthropicChatOptions.builder().model(judgeModel).temperature(0.0).build());
            case GEMINI    -> new Prompt(messages,
                    VertexAiGeminiChatOptions.builder().model(judgeModel).temperature(0.0).build());
            default        -> new Prompt(messages,
                    OpenAiChatOptions.builder().model(judgeModel).temperature(0.0).build());
        };
    }

    // 파싱 실패 시 예외 throw → EvalProcessor.judgeAndSave catch에서 해당 조합 skip
    private EvalScores parseScores(String judgeModel, String raw) {
        // Anthropic 등 일부 모델이 ```json ... ``` 펜스로 감싸서 응답하는 케이스 처리
        String json = stripCodeFence(raw);
        EvalScores scores;
        try {
            scores = objectMapper.readValue(json, EvalScores.class);
        } catch (Exception e) {
            log.warn("Judge 응답 파싱 실패, 해당 조합 skip: judgeModel={}, raw={}", judgeModel, raw);
            throw new IllegalStateException("Judge 응답 파싱 실패", e);
        }
        return validateRange(scores, judgeModel);
    }

    private String stripCodeFence(String text) {
        String s = text.trim();
        if (s.startsWith("```")) {
            s = s.replaceFirst("```(?:json)?\\s*", "");
            int end = s.lastIndexOf("```");
            if (end >= 0) s = s.substring(0, end);
        }
        return s.trim();
    }

    // 범위 이탈도 통계 오염 방지를 위해 예외 throw → skip
    private EvalScores validateRange(EvalScores scores, String judgeModel) {
        checkRange(scores.relevance(), judgeModel, "relevance");
        checkRange(scores.faithfulness(), judgeModel, "faithfulness");
        checkRange(scores.contextPrecision(), judgeModel, "contextPrecision");
        return scores;
    }

    private void checkRange(double value, String judgeModel, String field) {
        if (value < 1.0 || value > 5.0) {
            log.warn("Judge 점수 범위 이탈, 해당 조합 skip: judgeModel={}, field={}, value={}", judgeModel, field, value);
            throw new IllegalStateException("Judge 점수 범위 이탈: field=%s, value=%s".formatted(field, value));
        }
    }
}