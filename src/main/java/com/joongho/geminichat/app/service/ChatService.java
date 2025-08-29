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
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final WebClient webClient;
    private final String apiKey;
    private final String apiStreamUrl;
    private final String apiUrl;

    public ChatService(WebClient webClient,
                       @Value("${gemini.api.key}") String apiKey,
                       @Value("${gemini.api.url}") String apiUrl,
                       @Value("${gemini.api.stream-url}") String apiStreamUrl) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.apiStreamUrl = apiStreamUrl;
        this.apiUrl = apiUrl;
    }

    public Flux<String> getChatResponseStream(ChatDtos.ChatRequest request) {
        Map<String, Object> requestBody = createGeminiRequestBody(request.getHistory(), request.getPersona());

        System.out.println();

        return webClient.post()
                .uri(apiStreamUrl + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(Map.class)
                .map(this::extractTextFromResponse);
    }

    public Mono<ChatDtos.ChatResponse> getChatResponseSimple(ChatDtos.ChatRequest request) {
        // createGeminiRequestBody 메소드에 persona를 전달합니다.
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

    // Gemini API가 요구하는 형식에 맞게 대화 기록을 변환합니다.
    // persona 파라미터를 받도록 수정합니다.
    private Map<String, Object> createGeminiRequestBody(List<ChatDtos.MessageDto> history, String persona) {
        List<Map<String, Object>> contents = history.stream()
                .map(msg -> Map.of(
                        "role", msg.getRole(),
                        "parts", List.of(Map.of("text", msg.getText()))
                ))
                .toList();

        // 하드코딩된 페르소나 대신 전달받은 persona를 사용합니다.
        return Map.of(
                "contents", contents,
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", persona))
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
            // 스트리밍 중 가끔 빈 응답이 올 수 있으므로, 오류 대신 빈 문자열을 반환합니다.
        }
        return "";
    }
}
