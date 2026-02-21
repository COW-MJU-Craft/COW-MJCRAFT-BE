package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderViewToken;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderViewTokenRepository;
import com.example.cowmjucraft.global.config.AppProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderViewTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OrderViewTokenRepository orderViewTokenRepository;
    private final AppProperties appProperties;

    @Transactional
    public String issueNewToken(Order order, LocalDateTime now) {
        String rawToken = generateRawToken();
        orderViewTokenRepository.save(new OrderViewToken(
                order,
                hashToken(rawToken),
                now.plusMinutes(appProperties.getOrderViewTokenTtlMinutes()),
                null
        ));
        return rawToken;
    }

    @Transactional
    public String rotateToken(Order order, LocalDateTime now) {
        // 상태 변경 이후에는 항상 최신 주문 상태를 보도록 기존 링크를 폐기하고 새 토큰을 발급한다.
        orderViewTokenRepository.revokeActiveTokens(order.getId(), now);
        return issueNewToken(order, now);
    }

    public String hashToken(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new OrderException(OrderErrorType.TOKEN_HASH_FAILED);
        }
    }

    public String buildOrderViewUrl(String token) {
        String base = appProperties.getPublicBaseUrl();
        String path = appProperties.getOrderViewPath();

        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return base + path + "?token=" + token;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
