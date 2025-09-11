package com.joongho.geminichat.app.repository;

import com.joongho.geminichat.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
