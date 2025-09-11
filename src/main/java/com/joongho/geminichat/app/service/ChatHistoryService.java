package com.joongho.geminichat.app.service;

import com.joongho.geminichat.app.domain.ChatSession;
import com.joongho.geminichat.app.domain.User;
import com.joongho.geminichat.app.dto.ChatHistoryDtos;
import com.joongho.geminichat.app.repository.ChatSessionRepository;
import com.joongho.geminichat.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatHistoryService {

    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;

    /**
     * 특정 사용자의 모든 채팅 세션 목록을 최신순으로 조회합니다.
     * @param userId 사용자의 ID
     * @return 세션 요약 정보 DTO 리스트
     */
    public List<ChatHistoryDtos.SessionSummaryResponse> getUserSessions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        return chatSessionRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(session -> new ChatHistoryDtos.SessionSummaryResponse(
                        session.getId(),
                        session.getPersona(),
                        session.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * 특정 채팅 세션에 포함된 모든 메시지를 조회합니다.
     * @param sessionId 채팅 세션의 ID
     * @return 메시지 정보 DTO 리스트
     */
    public List<ChatHistoryDtos.MessageResponse> getSessionMessages(Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("채팅 세션을 찾을 수 없습니다: " + sessionId));

        return session.getMessages().stream()
                .map(message -> new ChatHistoryDtos.MessageResponse(
                        message.getId(),
                        message.getRole(),
                        message.getText(),
                        message.getCreatedAt()))
                .collect(Collectors.toList());
    }


}
