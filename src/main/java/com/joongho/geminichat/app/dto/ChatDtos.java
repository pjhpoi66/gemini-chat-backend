package com.joongho.geminichat.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ChatRequest {
        private String message;
    }

    @Getter
    public static class ChatResponse {
        private final String reply;

        public ChatResponse(String reply) {
            this.reply = reply;
        }
    }

}
