package com.example.cowmjucraft.domain.accounts.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cowmjucraft.domain.accounts.Role;
import com.example.cowmjucraft.domain.accounts.auth.entity.RefreshToken;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/**
 * 리프레시 토큰의 원자적 소비 쿼리를 검증한다.
 * 단일 소비 보장이 이 쿼리 하나에 달려 있어 실제 DB에서 확인한다.
 */
@DataJpaTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void consumeIfActive_활성토큰_1반환하고폐기된다() {
        // given
        RefreshToken token = save("hash-active", LocalDateTime.now().plusDays(7));
        flushAndClear();

        // when
        int affected = refreshTokenRepository.consumeIfActive("hash-active", LocalDateTime.now());
        flushAndClear();

        // then
        assertThat(affected).isEqualTo(1);
        assertThat(refreshTokenRepository.findById(token.getId()))
                .get()
                .extracting(RefreshToken::getRevokedAt)
                .isNotNull();
    }

    @Test
    void consumeIfActive_두번연속호출_두번째는0반환() {
        // given
        save("hash-once", LocalDateTime.now().plusDays(7));
        flushAndClear();

        // when — 동시 요청이 순차적으로 도달한 상황과 동일하다
        int first = refreshTokenRepository.consumeIfActive("hash-once", LocalDateTime.now());
        flushAndClear();
        int second = refreshTokenRepository.consumeIfActive("hash-once", LocalDateTime.now());
        flushAndClear();

        // then — 정확히 한 번만 성공해야 한다
        assertThat(first).isEqualTo(1);
        assertThat(second).isZero();
    }

    @Test
    void consumeIfActive_존재하지않는해시_0반환() {
        // when
        int affected = refreshTokenRepository.consumeIfActive("no-such-hash", LocalDateTime.now());

        // then
        assertThat(affected).isZero();
    }

    @Test
    void consumeIfActive_다른토큰에는영향없다() {
        // given
        save("hash-a", LocalDateTime.now().plusDays(7));
        RefreshToken other = save("hash-b", LocalDateTime.now().plusDays(7));
        flushAndClear();

        // when
        refreshTokenRepository.consumeIfActive("hash-a", LocalDateTime.now());
        flushAndClear();

        // then
        assertThat(refreshTokenRepository.findById(other.getId()))
                .get()
                .extracting(RefreshToken::getRevokedAt)
                .isNull();
    }

    private RefreshToken save(String tokenHash, LocalDateTime expiresAt) {
        return refreshTokenRepository.save(
                new RefreshToken("admin", Role.ROLE_ADMIN, tokenHash, expiresAt)
        );
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
