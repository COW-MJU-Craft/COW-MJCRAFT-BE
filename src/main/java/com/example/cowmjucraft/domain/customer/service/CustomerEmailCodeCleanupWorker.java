package com.example.cowmjucraft.domain.customer.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 만료된 인증 코드를 정리한다. 1회용 데이터라 남겨둘 이유가 없다.
 *
 * <p>여유를 두고 지우는 이유: 방금 만료된 코드를 즉시 지우면 사용자가 늦게 입력했을 때
 * "코드가 틀렸다" 대신 "코드가 아예 없다" 경로로 빠지는데, 두 경우 응답은 같지만
 * 실패 카운터가 쌓이지 않아 무한 재시도가 가능해진다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.customer-code-cleanup-enabled", havingValue = "true", matchIfMissing = true)
public class CustomerEmailCodeCleanupWorker {

    private static final int RETENTION_HOURS = 24;

    private final CustomerEmailCodeService customerEmailCodeService;

    @Scheduled(cron = "${app.customer-code-cleanup-cron:0 30 4 * * *}")
    public void purgeExpiredCodes() {
        int deleted = customerEmailCodeService.purgeExpired(LocalDateTime.now().minusHours(RETENTION_HOURS));
        if (deleted > 0) {
            log.info("만료된 이메일 인증 코드 정리: {}건", deleted);
        }
    }
}
