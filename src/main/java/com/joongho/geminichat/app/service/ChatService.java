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

        ChatMessage userMessage = ChatMessage.builder()
                .chatSession(session)
                .role("user")
                .text(text)
                .build();

        session.getMessages().add(userMessage);

        String modelResponseText = geminiApiService.generateResponse(session.getPersona(), session.getMessages());

        // 4. 모델 응답 메시지 객체 생성
        ChatMessage modelMessage = ChatMessage.builder()
                .chatSession(session)
                .role("model")
                .text(modelResponseText)
                .build();

        chatMessageRepository.save(userMessage);
        ChatMessage savedModelMessage = chatMessageRepository.save(modelMessage);

        return new ChatDtos.MessageResponse(savedModelMessage);
    }
}