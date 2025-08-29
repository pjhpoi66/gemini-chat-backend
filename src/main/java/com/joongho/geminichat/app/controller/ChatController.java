package com.joongho.geminichat.app.controller;

import com.joongho.geminichat.app.dto.ChatDtos;
import com.joongho.geminichat.app.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 새로 추가된 단일 응답 API
    @PostMapping("/simple")
    public Mono<ResponseEntity<ChatDtos.ChatResponse>> chatWithCharacterSimple(@RequestBody ChatDtos.ChatRequest request) {
        return chatService.getChatResponseSimple(request)
                .map(ResponseEntity::ok);
    }

    // 새로 추가된 스트리밍 응답 API
    @PostMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<String> chatWithCharacterStream(@RequestBody ChatDtos.ChatRequest request) {
        return chatService.getChatResponseStream(request);
    }
}
