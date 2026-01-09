package com.example.cowmjucraft.domain.accounts.admin.repository;

import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
}
