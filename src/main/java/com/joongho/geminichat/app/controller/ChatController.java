package com.joongho.geminichat.app.controller;

import com.joongho.geminichat.app.dto.ChatDtos;
import com.joongho.geminichat.app.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/simple")
    public ResponseEntity<Map<String, Long>> chatWithCharacterSimple(
            @RequestBody ChatDtos.ChatRequest request, Principal principal) {

        // 서비스의 비동기 메소드를 호출하고, 생성되거나 사용된 세션 ID를 받습니다.
        Long sessionId = chatService.initiateChatStream(request, principal);

        // 클라이언트가 스트림에 연결할 수 있도록 세션 ID를 반환합니다.
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }

    // streamChatResponse 메소드는 그대로 둡니다.
    @GetMapping(value = "/stream/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChatResponse(@PathVariable Long sessionId) {
        return chatService.addEmitter(sessionId);
    }
}