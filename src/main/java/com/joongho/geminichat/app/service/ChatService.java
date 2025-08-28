package com.joongho.geminichat.app.service;

import com.joongho.geminichat.app.dto.ChatDtos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final WebClient webClient;
    private final String apiKey;
    private final String apiUrl;
    private final String apiStreamUrl; // 스트리밍 URL 추가

    public ChatService(WebClient webClient,
                       @Value("${gemini.api.key}") String apiKey,
                       @Value("${gemini.api.url}") String apiUrl,
                       @Value("${gemini.api.stream-url}") String apiStreamUrl) { // 주입
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.apiStreamUrl = apiStreamUrl;
    }

    // 기존의 단일 응답 메소드
    public Mono<ChatDtos.ChatResponse> getChatResponse(ChatDtos.ChatRequest request) {
        String prompt = createPrompt(request.getMessage());
        Map<String, Object> requestBody = createRequestBody(prompt);

        return webClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractTextFromResponse)
                .map(ChatDtos.ChatResponse::new);
    }

    // 새로 추가된 스트리밍 응답 메소드
    public Flux<String> getChatResponseStream(ChatDtos.ChatRequest request) {
        String prompt = createPrompt(request.getMessage());
        Map<String, Object> requestBody = createRequestBody(prompt);

        return webClient.post()
                .uri(apiStreamUrl + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(Map.class) // 응답을 Flux(스트림) 형태로 받습니다.
                .map(this::extractTextFromResponse); // 각 데이터 조각에서 텍스트를 추출합니다.
    }

    // 프롬프트와 요청 본문을 만드는 헬퍼 메소드 (중복 제거)
    private String createPrompt(String message) {
        String characterPersona = "당신은 31살 고등학교 국어 교사입니다. 여성이며 단아한 말투를 사용합니다.";
        return characterPersona + " 다음 질문에 답해주세요: " + message;
    }

    private Map<String, Object> createRequestBody(String prompt) {
        return Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );
    }

    private String extractTextFromResponse(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                return (String) parts.get(0).get("text");
            }
        } catch (Exception e) {
            return "오류 ";
        }
        return "";
    }
}
