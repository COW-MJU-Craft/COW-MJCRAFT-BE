package com.example.cowmjucraft.domain.accounts.auth.repository;

import com.example.cowmjucraft.domain.accounts.Role;
import com.example.cowmjucraft.domain.accounts.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshToken t
               set t.revokedAt = :revokedAt
             where t.subject = :subject
               and t.role = :role
               and t.revokedAt is null
               and t.expiresAt > :revokedAt
            """)
    int revokeActiveTokens(
            @Param("subject") String subject,
            @Param("role") Role role,
            @Param("revokedAt") LocalDateTime revokedAt
    );
}
