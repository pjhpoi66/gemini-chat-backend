package com.joongho.geminichat.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AuthDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RegisterRequest {
        private String username;
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Getter
    public static class AuthResponse {
        private final String username;
        private final String token;
        private final String tokenType = "Bearer ";

        public AuthResponse(String username, String token) {
            this.username = username;
            this.token = token;
        }
    }

}
