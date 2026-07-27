package com.example.cowmjucraft.global.config.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 규칙 + 클라이언트 키 단위로 토큰 버킷을 관리한다.
 *
 * <p>단일 인스턴스 전제의 인메모리 구현이다. 스케일아웃 시에는 인스턴스별로
 * 카운터가 분리되므로 공유 저장소(Redis) 기반으로 교체해야 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitProperties properties;
    private final Map<String, Entry> buckets = new ConcurrentHashMap<>();

    /** 토큰을 소모하지 않고 남은 여유가 있는지만 확인한다. */
    public boolean hasCapacity(RateLimitRule rule, String clientKey) {
        RateLimitProperties.Limit limit = properties.limitFor(rule);
        if (limit == null) {
            return true;
        }
        return entry(rule, clientKey, limit).bucket().getAvailableTokens() > 0;
    }

    /** 토큰 하나를 소모한다. 남은 토큰이 없으면 아무 일도 하지 않는다. */
    public void consume(RateLimitRule rule, String clientKey) {
        RateLimitProperties.Limit limit = properties.limitFor(rule);
        if (limit == null) {
            return;
        }
        entry(rule, clientKey, limit).bucket().tryConsume(1);
    }

    /** 제한에 걸렸을 때 클라이언트에게 안내할 재시도 대기 시간(초). */
    public long retryAfterSeconds(RateLimitRule rule) {
        RateLimitProperties.Limit limit = properties.limitFor(rule);
        return limit == null ? 60L : Math.max(1L, limit.getWindow().toSeconds());
    }

    private Entry entry(RateLimitRule rule, String clientKey, RateLimitProperties.Limit limit) {
        Entry entry = buckets.computeIfAbsent(
                rule.getKey() + '|' + clientKey,
                ignored -> new Entry(newBucket(limit))
        );
        entry.touch();
        return entry;
    }

    private Bucket newBucket(RateLimitProperties.Limit limit) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit.getCapacity())
                        .refillGreedy(limit.getCapacity(), limit.getWindow())
                        .build())
                .build();
    }

    /**
     * 유휴 버킷을 정리한다. 클라이언트 키가 IP 단위라 방치하면 메모리가 계속 늘어난다.
     */
    @Scheduled(fixedDelayString = "${app.rate-limit.eviction-interval-ms:600000}")
    public void evictIdleBuckets() {
        Duration idleEviction = properties.getIdleEviction();
        Instant threshold = Instant.now().minus(idleEviction);

        int before = buckets.size();
        buckets.entrySet().removeIf(entry -> entry.getValue().lastAccessedAt().isBefore(threshold));
        int removed = before - buckets.size();

        if (removed > 0) {
            log.debug("요청 제한 버킷 정리: 제거={}, 잔여={}", removed, buckets.size());
        }
    }

    private static final class Entry {

        private final Bucket bucket;
        private volatile Instant lastAccessedAt;

        private Entry(Bucket bucket) {
            this.bucket = bucket;
            this.lastAccessedAt = Instant.now();
        }

        private Bucket bucket() {
            return bucket;
        }

        private Instant lastAccessedAt() {
            return lastAccessedAt;
        }

        private void touch() {
            this.lastAccessedAt = Instant.now();
        }
    }
}
