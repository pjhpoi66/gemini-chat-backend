package com.joongho.geminichat.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GeminiDtos {

    // --- Gemini API 요청 DTO ---
    public record GeminiRequest(
            List<Content> contents,
            @JsonProperty("system_instruction")
            Content systemInstruction
    ) {}

    // --- Gemini API 응답 DTO ---
    public record GeminiResponse(
            List<Candidate> candidates
    ) {}

    // --- 공통 내부 DTO ---
    public record Content(
            String role,
            List<Part> parts
    ) {}

    public record Part(
            String text
    ) {}

    public record Candidate(
            Content content
    ) {}

}
