package com.example.cowmjucraft.domain.auth.signin.repository;

import com.example.cowmjucraft.domain.auth.signin.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUserId(String userId);
}
