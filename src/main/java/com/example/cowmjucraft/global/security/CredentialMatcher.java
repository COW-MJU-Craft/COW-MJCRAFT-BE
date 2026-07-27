package com.example.cowmjucraft.global.security;

import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 자격증명 대조 시 응답 시간을 균일하게 유지한다.
 *
 * <p>대상이 없을 때 즉시 실패하면 해시 연산에 걸리는 시간만큼 차이가 생겨,
 * 응답 시간만으로 계정·주문·지원서의 존재 여부를 알아낼 수 있다.
 * 저장된 해시가 없어도 더미 해시로 같은 연산을 수행해 시간 차이를 없앤다.
 */
@Component
public class CredentialMatcher {

    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public CredentialMatcher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    /**
     * @param storedHash 저장된 해시. 대상이 없으면 {@code null}을 넘긴다.
     * @return 일치 여부. {@code storedHash}가 {@code null}이면 항상 {@code false}
     */
    public boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null) {
            passwordEncoder.matches("", dummyHash);
            return false;
        }
        if (storedHash == null) {
            passwordEncoder.matches(rawPassword, dummyHash);
            return false;
        }
        return passwordEncoder.matches(rawPassword, storedHash);
    }
}
