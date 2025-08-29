package com.joongho.geminichat.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class ChatDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ChatRequest {
        // 기존 message 필드 대신 history 리스트를 받습니다.
        private List<MessageDto> history;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MessageDto {
        private String role; // "user" 또는 "model"
        private String text;
    }

    @Getter
    @Setter
    public static class ChatResponse {
        private String response;

        public ChatResponse(String response) {
            this.response = response;
        }
    }
}
