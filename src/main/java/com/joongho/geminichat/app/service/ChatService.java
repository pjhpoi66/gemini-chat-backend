package com.joongho.geminichat.app.service;

import com.joongho.geminichat.app.dto.ChatDtos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService {

    private final WebClient webClient;
    private final String apiKey;
    private final String apiUrl;

    // 기억할 최대 메시지 개수(사용자 질문 + AI 답변 = 2)를 상수로 정의
    // MAX_HISTORY_SIZE = 20 이면, 최근 10번의 대화를 기억합니다.
    private static final int MAX_HISTORY_SIZE = 20;

    public ChatService(WebClient webClient,
                       @Value("${gemini.api.key}") String apiKey,
                       @Value("${gemini.api.url}") String apiUrl) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public Mono<ChatDtos.ChatResponse> getChatResponseSimple(ChatDtos.ChatRequest request) {
        Map<String, Object> requestBody = createGeminiRequestBody(request.getHistory(), request.getPersona());

        return webClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractTextFromResponse)
                .map(ChatDtos.ChatResponse::new);
    }

    private Map<String, Object> createGeminiRequestBody(List<ChatDtos.MessageDto> history, String persona) {

        // Sliding Window 로직 추가
        List<ChatDtos.MessageDto> recentHistory = history;
        if (history.size() > MAX_HISTORY_SIZE) {
            // 전체 대화 기록이 최대치보다 크면, 가장 최근 N개만 잘라냅니다.
            int startIndex = history.size() - MAX_HISTORY_SIZE;
            recentHistory = history.subList(startIndex, history.size());
        }

        // 이제 전체 history 대신, 잘라낸 recentHistory를 사용합니다.
        List<Map<String, Object>> contents = recentHistory.stream()
                .map(msg -> Map.of(
                        "role", msg.getRole(),
                        "parts", List.of(Map.of("text", msg.getText()))
                ))
                .collect(Collectors.toList());

        Map<String, Object> generationConfig = Map.of(
                "maxOutputTokens", 2048,
                "temperature", 0.9,
                "topP", 1.0
        );

        List<Map<String, String>> safetySettings = List.of(
                Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_NONE"),
                Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_NONE"),
                Map.of("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold", "BLOCK_NONE"),
                Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_NONE")
        );

        return Map.of(
                "contents", contents,
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", persona))
                ),
                "generationConfig", generationConfig,
                "safetySettings", safetySettings
        );
    }

    private String extractTextFromResponse(Map<String, Object> responseBody) {
        log.info("Gemini API Response: {}", responseBody);

        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                if (content != null) {
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            log.error("extractTextFromResponse error!!", e);
        }
        return "죄송해요, 답변을 생성하는 데 문제가 생겼어요.";
    }
}