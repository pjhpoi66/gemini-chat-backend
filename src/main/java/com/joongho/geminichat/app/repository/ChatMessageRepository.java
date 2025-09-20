package com.joongho.geminichat.app.repository;

import com.joongho.geminichat.app.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatSessionId(Long sessionId);
}
