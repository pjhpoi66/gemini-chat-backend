package com.joongho.geminichat.app.service;

import com.joongho.geminichat.app.domain.ChatMessage;
import com.joongho.geminichat.app.dto.GeminiDtos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeminiApiService {

    private final WebClient webClient;
    private final String geminiUrl;
    private final String geminiApiKey;

    public GeminiApiService(WebClient.Builder webClientBuilder,
                            @Value("${gemini.api.url}") String geminiUrl,
                            @Value("${gemini.api.key}") String geminiApiKey) {
        this.webClient = webClientBuilder.build();
        this.geminiUrl = geminiUrl;
        this.geminiApiKey = geminiApiKey;
    }

    public String generateResponse(String persona, List<ChatMessage> history) {
        log.info("Requesting response from Gemini API... ");

        // 1. 페르소나 설정 (System Instruction)
        GeminiDtos.Content systemInstruction = new GeminiDtos.Content(
                null,
                List.of(new GeminiDtos.Part(persona))
        );

        // 2. 대화 기록을 Gemini API 형식으로 변환
        List<GeminiDtos.Content> contents = history.stream()
                .map(message -> new GeminiDtos.Content(
                        message.getRole(),
                        List.of(new GeminiDtos.Part(message.getText()))
                ))
                .collect(Collectors.toList());

        // 3. Gemini API 요청 객체 생성
        GeminiDtos.GeminiRequest requestBody = new GeminiDtos.GeminiRequest(contents, systemInstruction);

        // 4. WebClient를 사용하여 API 호출
        try {
            GeminiDtos.GeminiResponse response = webClient.post()
                    .uri(geminiUrl + "?key=" + geminiApiKey)
                    .header("Content-Type", "application/json")
                    .body(Mono.just(requestBody), GeminiDtos.GeminiRequest.class)
                    .retrieve() // 응답을 받기 시작
                    .bodyToMono(GeminiDtos.GeminiResponse.class)
                    .block();

            // 5. 응답에서 텍스트 추출
            if (response != null && !response.candidates().isEmpty()) {
                String responseText = response.candidates().getFirst().content().parts().getFirst().text();
                log.info("Received response from Gemini API.");
                return responseText;
            } else {
                log.error("No candidates found in Gemini API response.");
                return "죄송해요, 응답을 생성할 수 없어요.";
            }
        } catch (Exception e) {
            log.error("Error while calling Gemini API: {}", e.getMessage(), e);
            return "API 호출 중 오류가 발생했습니다.";
        }
    }

}
