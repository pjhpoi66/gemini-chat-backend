package com.joongho.geminichat.app.controller;

import com.joongho.geminichat.app.dto.ChatHistoryDtos;
import com.joongho.geminichat.app.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    /**
     * 특정 사용자의 모든 채팅 세션 목록을 조회하는 API
     * @param userId 사용자의 ID
     * @return 세션 요약 목록
     */
    @GetMapping("/sessions/user/{userId}")
    public ResponseEntity<List<ChatHistoryDtos.SessionSummaryResponse>> getUserChatSessions(@PathVariable Long userId) {
        List<ChatHistoryDtos.SessionSummaryResponse> sessions = chatHistoryService.getUserSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * 특정 채팅 세션의 모든 메시지를 조회하는 API
     * @param sessionId 채팅 세션의 ID
     * @return 메시지 목록
     */
    @GetMapping("/messages/session/{sessionId}")
    public ResponseEntity<List<ChatHistoryDtos.MessageResponse>> getChatSessionMessages(@PathVariable Long sessionId) {
        List<ChatHistoryDtos.MessageResponse> messages = chatHistoryService.getSessionMessages(sessionId);
        return ResponseEntity.ok(messages);
    }
}
