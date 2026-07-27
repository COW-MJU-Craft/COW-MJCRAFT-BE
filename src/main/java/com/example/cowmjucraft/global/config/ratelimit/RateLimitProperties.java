package com.example.cowmjucraft.global.config.ratelimit;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    /** 요청 제한 전체 on/off. 장애 시 재배포 없이 끌 수 있도록 둔다. */
    private boolean enabled = true;

    /** 사용하지 않는 버킷을 정리하기까지의 유휴 시간. */
    private Duration idleEviction = Duration.ofMinutes(30);

    /** {@link RateLimitRule#getKey()} → 임계값 */
    private Map<String, Limit> rules = new LinkedHashMap<>();

    public Limit limitFor(RateLimitRule rule) {
        return rules.get(rule.getKey());
    }

    @Getter
    @Setter
    public static class Limit {

        @Min(value = 1, message = "capacity must be >= 1")
        private int capacity;

        @NotNull(message = "window must not be null")
        private Duration window;
    }
}
