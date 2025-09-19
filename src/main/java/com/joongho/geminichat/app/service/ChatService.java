// src/main/java/com/joongho/geminichat/app/service/ChatService.java

package com.joongho.geminichat.app.service;

import com.joongho.geminichat.app.domain.ChatMessage;
import com.joongho.geminichat.app.domain.ChatSession;
import com.joongho.geminichat.auth.domain.User;
import com.joongho.geminichat.app.dto.ChatDtos;
import com.joongho.geminichat.app.repository.ChatMessageRepository;
import com.joongho.geminichat.app.repository.ChatSessionRepository;
import com.joongho.geminichat.auth.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService {

    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final WebClient webClient;
    private final String apiKey;
    private final String apiUrl;
    private final ChatService self; // ◀◀ @Async 호출을 위한 자기 자신 주입

    private static final int MAX_HISTORY_SIZE = 20;

    // SseEmitter를 세션 ID별로 저장하기 위한 스레드-안전한 맵
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // @Lazy 어노테이션으로 순환 참조 문제 해결
    public ChatService(UserRepository userRepository, ChatSessionRepository chatSessionRepository, ChatMessageRepository chatMessageRepository, WebClient webClient,
                       @Value("${gemini.api.key}") String apiKey,
                       @Value("${gemini.api.url}") String apiUrl,
                       @Lazy @Autowired ChatService self) {
        this.userRepository = userRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.self = self;
    }

    // --- ⬇⬇ 새로 추가된 SSE 관련 메소드들 ⬇⬇ ---

    /**
     * 클라이언트가 SSE 스트림 연결을 요청할 때 호출됩니다.
     * @param sessionId 연결을 식별할 세션 ID
     * @return 생성된 SseEmitter 객체
     */
    public SseEmitter addEmitter(Long sessionId) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5분 타임아웃
        emitters.put(sessionId, emitter);
        log.info("새로운 Emitter 추가됨. Session ID: {}", sessionId);

        // 타임아웃 또는 완료 시 맵에서 Emitter 제거
        emitter.onCompletion(() -> {
            log.info("Emitter 완료됨. Session ID: {}", sessionId);
            emitters.remove(sessionId);
        });
        emitter.onTimeout(() -> {
            log.info("Emitter 타임아웃. Session ID: {}", sessionId);
            emitters.remove(sessionId);
        });
        emitter.onError(e -> {
            log.error("Emitter 오류 발생. Session ID: {}", sessionId, e);
            emitters.remove(sessionId);
        });

        // 연결 확인을 위해 초기 더미 이벤트 전송
        try {
            emitter.send(SseEmitter.event().name("connect").data("SSE connection established."));
        } catch (IOException e) {
            log.error("SSE 초기 연결 이벤트 전송 실패", e);
        }

        return emitter;
    }

    /**
     * Controller에서 호출할 비동기 작업 시작 메소드.
     * 이 메소드는 내부적으로 @Async가 붙은 메소드를 호출합니다.
     */
    public Long initiateChatStream(ChatDtos.ChatRequest request, Principal principal) {
        // 이 메소드 안에서 세션을 찾거나 생성하는 로직이 이미 있습니다.
        // 해당 세션의 ID를 반환해주기만 하면 됩니다.
        ChatSession session = findOrCreateSession(request, principal);

        // 비동기 처리를 시작합니다.
        self.processChatAndStream(request, principal, session);

        // 생성된 세션의 ID를 컨트롤러로 반환합니다.
        return session.getId();
    }

    /**
     * 실제 장기 실행 작업을 수행할 비동기 메소드.
     * 트랜잭션과 비동기 처리가 함께 적용됩니다.
     */
    @Async
    @Transactional
    public void processChatAndStream(ChatDtos.ChatRequest request, Principal principal, ChatSession session) {
        // 1. 세션을 가져오거나 새로 생성하고, 사용자의 새 메시지를 DB에 저장합니다.
        //ChatSession session = findOrCreateSession(request, principal);
        String userMessageText = request.getHistory().getLast().getText();
        saveMessage(session, "user", userMessageText);

        Long sessionId = session.getId();
        SseEmitter emitter = emitters.get(sessionId);

        if (emitter == null) {
            log.warn("Session ID {}에 해당하는 Emitter가 존재하지 않아 스트리밍을 중단합니다.", sessionId);
            return;
        }

        try {
            // 2. Gemini API 요청 본문을 생성합니다.
            Map<String, Object> requestBody = createGeminiRequestBody(request.getHistory(), session.getPersona());

            // 3. WebClient를 호출하고, block()을 사용하여 동기적으로 결과를 기다립니다.
            //    (@Async 스레드 안이므로 메인 스레드를 블로킹하지 않습니다.)
            Map<String, Object> responseBody = webClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // 4. Gemini 응답을 추출하고 DB에 저장합니다.
            String aiResponse = extractTextFromResponse(responseBody);
            saveMessage(session, "model", aiResponse);

            // 5. 생성된 최종 응답 DTO를 SSE Emitter를 통해 클라이언트로 전송합니다.
            ChatDtos.ChatResponse responseDto = new ChatDtos.ChatResponse(session.getId(), aiResponse);
            emitter.send(SseEmitter.event().name("message").data(responseDto));

        } catch (Exception e) {
            log.error("채팅 스트림 처리 중 예외 발생. Session ID: {}", sessionId, e);
            // 클라이언트에 에러 이벤트 전송
            try {
                emitter.send(SseEmitter.event().name("error").data("Error processing your request."));
            } catch (IOException ioException) {
                log.error("SSE 에러 이벤트 전송 실패", ioException);
            }
            emitter.completeWithError(e);
        } finally {
            // 성공적으로 응답을 보낸 후 연결을 정상적으로 종료합니다.
            emitter.complete();
        }
    }

    // --- ⬇⬇ 기존의 private 헬퍼 메소드들은 그대로 유지합니다 ⬇⬇ ---

    private Map<String, Object> createGeminiRequestBody(List<ChatDtos.MessageDto> history, String persona) {
        List<ChatDtos.MessageDto> recentHistory = history;
        if (history.size() > MAX_HISTORY_SIZE) {
            int startIndex = history.size() - MAX_HISTORY_SIZE;
            recentHistory = history.subList(startIndex, history.size());
        }

        List<Map<String, Object>> contents = recentHistory.stream()
                .map(msg -> Map.of(
                        "role", msg.getRole(),
                        "parts", List.of(Map.of("text", msg.getText()))
                ))
                .toList();

        Map<String, Object> generationConfig = Map.of("maxOutputTokens", 2048, "temperature", 0.9, "topP", 1.0);
        List<Map<String, String>> safetySettings = List.of(
                Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_NONE"),
                Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_NONE"),
                Map.of("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold", "BLOCK_NONE"),
                Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_NONE")
        );

        return Map.of(
                "contents", contents,
                "systemInstruction", Map.of("parts", List.of(Map.of("text", persona))),
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
                        return (String) parts.getFirst().get("text");
                    }
                }
            }
        } catch (Exception e) {
            log.error("extractTextFromResponse error!!", e);
        }
        return "죄송해요, 답변을 생성하는 데 문제가 생겼어요.";
    }

    private ChatSession findOrCreateSession(ChatDtos.ChatRequest request, Principal principal) {
        if (request.getSessionId() != null) {
            return chatSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new EntityNotFoundException("채팅 세션을 찾을 수 없습니다: " + request.getSessionId()));
        } else {
            if (request.getPersona() == null || request.getPersona().isBlank()) {
                throw new IllegalArgumentException("새로운 채팅을 시작하려면 페르소나가 반드시 필요합니다.");
            }
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("인증된 사용자를 찾을 수 없습니다: " + username));

            ChatSession newSession = new ChatSession();
            newSession.setUser(user);
            newSession.setPersona(request.getPersona());
            return chatSessionRepository.save(newSession);
        }
    }

    private void saveMessage(ChatSession session, String role, String text) {
        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setRole(role);
        message.setText(text);
        chatMessageRepository.save(message);
    }


}