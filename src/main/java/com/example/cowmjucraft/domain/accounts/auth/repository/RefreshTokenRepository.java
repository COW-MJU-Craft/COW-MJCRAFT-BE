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

    /**
     * 미폐기 상태일 때만 폐기해 토큰을 원자적으로 소비한다.
     *
     * <p>조회 후 검사하고 변경하는 방식은 동시 요청 둘이 모두 미폐기 상태를 읽어
     * 각각 새 토큰 쌍을 발급받을 수 있다. 조건을 UPDATE 문에 넣어 DB가 한 번만
     * 성공하도록 보장한다.
     *
     * @return 1이면 이번 호출이 소비에 성공한 것, 0이면 이미 소비됐거나 존재하지 않는다
     */
    // clearAutomatically는 쓰지 않는다 — 벌크 대상뿐 아니라 persistence context 전체를
    // detach시켜 이후 dirty checking이 동작하지 않는다.
    @Modifying(flushAutomatically = true)
    @Query("""
            update RefreshToken t
               set t.revokedAt = :now
             where t.tokenHash = :tokenHash
               and t.revokedAt is null
            """)
    int consumeIfActive(
            @Param("tokenHash") String tokenHash,
            @Param("now") LocalDateTime now
    );

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
