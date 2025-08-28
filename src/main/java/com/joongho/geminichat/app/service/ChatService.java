package com.joongho.geminichat.app.service;

import com.joongho.geminichat.app.dto.ChatDtos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final WebClient webClient;
    private final String apiKey;
    private final String apiUrl;

    // WebClient와 application.properties의 값을 주입받습니다.
    public ChatService(WebClient webClient,
                       @Value("${gemini.api.key}") String apiKey,
                       @Value("${gemini.api.url}") String apiUrl) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    // Gemini API와 통신하는 로직이 여기에 들어갑니다.
    public Mono<ChatDtos.ChatResponse> getChatResponse(ChatDtos.ChatRequest request) {

        // 1. 캐릭터 설정을 포함한 최종 프롬프트를 만듭니다.
        // 이 부분을 수정하여 원하는 캐릭터의 페르소나를 설정할 수 있습니다.
        String characterPersona = "당신은 40대 중년남성 만물박사 챗봇입니다. 모든 대답을 귀찮다는 듯이 하지만, 마지막엔 항상 '...흥!'이라고 붙여서 말해주세요.";
        String prompt = characterPersona + " 다음 질문에 답해주세요: " + request.getMessage();

        // 2. Gemini API에 보낼 요청 본문(body)을 구성합니다.
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        // 3. WebClient를 사용하여 Gemini API를 비동기적으로 호출합니다.
        return webClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve() // 응답을 받습니다.
                .bodyToMono(Map.class) // 응답 본문을 Map 형태로 변환합니다.
                .map(this::extractTextFromResponse) // 응답에서 실제 텍스트만 추출합니다.
                .map(ChatDtos.ChatResponse::new); // 추출된 텍스트로 ChatResponse 객체를 만듭니다.
    }

    // Gemini API 응답 JSON 구조에서 텍스트 부분만 추출하는 헬퍼 메소드
    private String extractTextFromResponse(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                return (String) parts.get(0).get("text");
            }
        } catch (Exception e) {
            // 복잡한 JSON 구조를 파싱하다 에러가 발생할 수 있습니다.
            return "응답을 처리하는 중 오류가 발생했습니다.";
        }
        return "죄송해요, 답변을 생성할 수 없어요.";
    }

}
