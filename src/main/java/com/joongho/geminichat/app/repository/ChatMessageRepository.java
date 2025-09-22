package com.joongho.geminichat.app.repository;

import com.joongho.geminichat.app.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
