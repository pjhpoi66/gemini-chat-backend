package com.joongho.geminichat.app.controller;

import com.joongho.geminichat.app.dto.ChatDtos;
import com.joongho.geminichat.app.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public Mono<ResponseEntity<ChatDtos.ChatResponse>> chatWithCharacter(@RequestBody ChatDtos.ChatRequest request) {
        return chatService.getChatResponse(request)
                .map(response -> ResponseEntity.ok(response));
    }
}
