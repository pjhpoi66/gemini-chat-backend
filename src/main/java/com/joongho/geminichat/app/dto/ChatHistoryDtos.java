package com.joongho.geminichat.app.dto;

import lombok.Getter;

import java.time.LocalDateTime;

public class ChatHistoryDtos {

    @Getter
    public static class SessionSummaryResponse {
        private final Long id;
        private final String persona;
        private final LocalDateTime createdAt;

        public SessionSummaryResponse(Long id, String persona, LocalDateTime createdAt) {
            this.id = id;
            this.persona = persona;
            this.createdAt = createdAt;
        }
    }

    @Getter
    public static class MessageResponse {
        private final Long id;
        private final String role;
        private final String text;
        private final LocalDateTime createdAt;

        public MessageResponse(Long id, String role, String text, LocalDateTime createdAt) {
            this.id = id;
            this.role = role;
            this.text = text;
            this.createdAt = createdAt;
        }
    }
}
