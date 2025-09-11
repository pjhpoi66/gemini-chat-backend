package com.joongho.geminichat.app.repository;

import com.joongho.geminichat.app.domain.ChatSession;
import com.joongho.geminichat.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByUserOrderByCreatedAtDesc(User user);
}
