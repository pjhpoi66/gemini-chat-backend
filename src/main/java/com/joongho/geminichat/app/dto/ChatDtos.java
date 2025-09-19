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
        private String persona; // 새로운 대화 시작 시 사용할 페르소나
        private Long sessionId; // 기존 대화를 이어갈 때 사용하는 세션 ID (새 대화일 경우 null)
        private List<MessageDto> history; // Gemini API에 보낼 대화 기록
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MessageDto {
        private String role;
        private String text;
    }

    @Getter
    @Setter
    public static class ChatResponse {
        private Long sessionId; // 프론트엔드가 다음 요청에 사용할 수 있도록 세션 ID를 응답에 포함

        private String response;

        public ChatResponse(Long sessionId, String response) {
            this.sessionId = sessionId;
            this.response = response;
        }
    }
}
