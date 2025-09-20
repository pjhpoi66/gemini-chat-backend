package com.joongho.geminichat.app.dto;

import com.joongho.geminichat.app.domain.ChatMessage;
import com.joongho.geminichat.app.domain.ChatSession;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ChatDtos {

    // --- 요청(Request) DTO ---

    @Getter
    @NoArgsConstructor
    public static class CreateRequest {
        private String persona;
    }

    @Getter
    @NoArgsConstructor
    public static class MessageRequest {
        private String text;
    }


    // --- 응답(Response) DTO ---

    @Getter
    public static class SessionResponse {
        private final Long id;
        private final String persona;
        private final LocalDateTime createdAt;

        public SessionResponse(ChatSession session) {
            this.id = session.getId();
            this.persona = session.getPersona();
            this.createdAt = session.getCreatedAt();
        }
    }

    @Getter
    public static class MessageResponse {
        private final Long id;
        private final String role;
        private final String text;
        private final LocalDateTime createdAt;

        public MessageResponse(ChatMessage message) {
            this.id = message.getId();
            this.role = message.getRole();
            this.text = message.getText();
            this.createdAt = message.getCreatedAt();
        }
    }

    // 채팅방의 모든 메시지를 담는 응답 DTO
    @Getter
    public static class MessageHistoryResponse {
        private final Long sessionId;
        private final String persona;
        private final List<MessageResponse> messages;

        public MessageHistoryResponse(ChatSession session) {
            this.sessionId = session.getId();
            this.persona = session.getPersona();
            this.messages = session.getMessages().stream()
                    .map(MessageResponse::new)
                    .collect(Collectors.toList());
        }
    }
}
