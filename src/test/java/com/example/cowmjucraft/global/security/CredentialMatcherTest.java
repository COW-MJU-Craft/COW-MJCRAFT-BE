package com.example.cowmjucraft.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class CredentialMatcherTest {

    private PasswordEncoder passwordEncoder;
    private CredentialMatcher credentialMatcher;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        credentialMatcher = new CredentialMatcher(passwordEncoder);
    }

    @Test
    void matches_비밀번호일치_true반환() {
        // given
        String storedHash = passwordEncoder.encode("Pa$$w0rd!");

        // when & then
        assertThat(credentialMatcher.matches("Pa$$w0rd!", storedHash)).isTrue();
    }

    @Test
    void matches_비밀번호불일치_false반환() {
        // given
        String storedHash = passwordEncoder.encode("Pa$$w0rd!");

        // when & then
        assertThat(credentialMatcher.matches("wrong", storedHash)).isFalse();
    }

    @Test
    void matches_저장된해시없음_false반환() {
        // when & then
        assertThat(credentialMatcher.matches("anything", null)).isFalse();
    }

    @Test
    void matches_입력비밀번호가null_false반환() {
        // given
        String storedHash = passwordEncoder.encode("Pa$$w0rd!");

        // when & then
        assertThat(credentialMatcher.matches(null, storedHash)).isFalse();
    }

    @Test
    void matches_대상없을때도_해시연산시간이유지된다() {
        // given — 존재하는 계정의 실패 대조와 존재하지 않는 계정의 대조 시간을 비교한다
        String storedHash = passwordEncoder.encode("Pa$$w0rd!");
        warmUp(storedHash);

        // when
        long existingNanos = measure(() -> credentialMatcher.matches("wrong", storedHash));
        long missingNanos = measure(() -> credentialMatcher.matches("wrong", null));

        // then — 대상이 없을 때가 유의미하게 빠르면 타이밍으로 존재 여부가 새어나간다.
        // bcrypt 연산 시간의 절반 이상은 소요되어야 한다.
        assertThat(missingNanos).isGreaterThan(existingNanos / 2);
    }

    private void warmUp(String storedHash) {
        for (int i = 0; i < 3; i++) {
            credentialMatcher.matches("wrong", storedHash);
            credentialMatcher.matches("wrong", null);
        }
    }

    private long measure(Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        return System.nanoTime() - start;
    }
}
