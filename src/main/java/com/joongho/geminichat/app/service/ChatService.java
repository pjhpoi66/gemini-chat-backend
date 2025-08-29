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
        List<Map<String, Object>> contents = history.stream()
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

        // 안전 설정 추가: 유해성 콘텐츠 차단 임계값을 완화합니다.
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