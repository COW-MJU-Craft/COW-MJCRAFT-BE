package com.example.mj_craft.repository;

import com.example.mj_craft.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUserId(String userId);
}
