package com.joongho.geminichat.app.service;

import com.joongho.geminichat.app.domain.ChatMessage;
import com.joongho.geminichat.app.domain.ChatSession;
import com.joongho.geminichat.app.dto.ChatDtos;
import com.joongho.geminichat.app.repository.ChatMessageRepository;
import com.joongho.geminichat.app.repository.ChatSessionRepository;
import com.joongho.geminichat.auth.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GeminiApiService geminiApiService;

    // 1. 새 채팅방 만들기
    @Transactional
    public ChatSession createChatSession(User user, String persona) {
        ChatSession newSession = ChatSession.builder()
                .user(user)
                .persona(persona)
                .build();
        return chatSessionRepository.save(newSession);
    }

    // 2. 기존 채팅방 목록 불러오기
    public List<ChatDtos.SessionResponse> findChatSessionsByUser(User user) {
        return chatSessionRepository.findByUserId(user.getId()).stream()
                .map(ChatDtos.SessionResponse::new)
                .collect(Collectors.toList());
    }

    // 3. 기존 채팅방 대화 기록 불러오기
    public ChatDtos.MessageHistoryResponse findMessagesBySessionId(Long sessionId, User user) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 본인의 채팅방이 맞는지 확인 (중요!)
        if (!session.getUser().getId().equals(user.getId())) {
            throw new SecurityException("접근 권한이 없습니다.");
        }

        return new ChatDtos.MessageHistoryResponse(session);
    }

    // 4. 새 메시지 보내기 (Gemini API 연동 전)
    @Transactional
    public ChatDtos.MessageResponse postMessage(Long sessionId, User user, String text) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new SecurityException("접근 권한이 없습니다.");
        }

        // --- 여기부터 로직을 수정합니다 ---

        // 1. 사용자 메시지 객체 생성 (아직 DB에 저장하지 않음)
        ChatMessage userMessage = ChatMessage.builder()
                .chatSession(session)
                .role("user")
                .text(text)
                .build();

        // 2. 현재 세션의 메시지 리스트에 방금 만든 사용자 메시지를 추가
        //    이렇게 하면 API에 보낼 대화 기록이 완벽해집니다.
        session.getMessages().add(userMessage);

        // 3. 완성된 대화 기록(session.getMessages())을 Gemini API로 전송
        String modelResponseText = geminiApiService.generateResponse(session.getPersona(), session.getMessages());

        // 4. 모델 응답 메시지 객체 생성
        ChatMessage modelMessage = ChatMessage.builder()
                .chatSession(session)
                .role("model")
                .text(modelResponseText)
                .build();

        // 5. 사용자 메시지와 모델 메시지를 모두 DB에 저장
        //    ChatSession 엔티티의 messages 필드에 CascadeType.ALL이 설정되어 있으므로,
        //    연관된 session 객체만 저장해도 message들이 함께 저장됩니다.
        //    하지만 명시적으로 chatMessageRepository를 사용해도 좋습니다.
        chatMessageRepository.save(userMessage);
        ChatMessage savedModelMessage = chatMessageRepository.save(modelMessage);

        return new ChatDtos.MessageResponse(savedModelMessage);
    }
}