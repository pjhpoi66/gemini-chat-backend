package com.joongho.geminichat.app.controller;

import com.joongho.geminichat.app.domain.ChatSession;
import com.joongho.geminichat.app.dto.ChatDtos;
import com.joongho.geminichat.app.service.ChatService;
import com.joongho.geminichat.auth.domain.User;
import com.joongho.geminichat.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ChatDtos.SessionResponse> createChat(
            Authentication authentication,
            @RequestBody ChatDtos.CreateRequest request) {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ChatSession newSession = chatService.createChatSession(user, request.getPersona());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ChatDtos.SessionResponse(newSession));
    }

    @GetMapping
    public ResponseEntity<List<ChatDtos.SessionResponse>> getChatSessions(Authentication authentication) {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<ChatDtos.SessionResponse> sessions = chatService.findChatSessionsByUser(user);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<ChatDtos.MessageHistoryResponse> getChatHistory(
            Authentication authentication,
            @PathVariable Long sessionId) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ChatDtos.MessageHistoryResponse history = chatService.findMessagesBySessionId(sessionId, user);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{sessionId}/messages")
    public ResponseEntity<ChatDtos.MessageResponse> postMessage(
            Authentication authentication,
            @PathVariable Long sessionId,
            @RequestBody ChatDtos.MessageRequest request) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ChatDtos.MessageResponse response = chatService.postMessage(sessionId, user, request.getText());
        return ResponseEntity.ok(response);
    }
}