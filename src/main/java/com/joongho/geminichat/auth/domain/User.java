package com.joongho.geminichat.auth.domain;

import com.joongho.geminichat.app.domain.ChatSession;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ◀◀ 타입을 String에서 Long으로 수정했습니다.

    @Column(nullable = false, length = 50, unique = true) // unique = true 추가
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatSession> sessions = new ArrayList<>();

    // AuthService에서 사용할 수 있도록 생성자 추가
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
