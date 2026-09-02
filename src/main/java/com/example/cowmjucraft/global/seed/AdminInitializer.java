package com.example.cowmjucraft.global.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 기동 완료 시점에 관리자 시딩을 트리거한다.
 *
 * <p>시딩 로직 자체는 {@link AdminSeeder}에 둔다. 같은 Bean 안에서
 * {@code @Transactional} 메서드를 호출하면 proxy를 우회해 트랜잭션이 적용되지 않는다.
 */
@RequiredArgsConstructor
@Component
public class AdminInitializer {

    private final AdminSeeder adminSeeder;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        adminSeeder.seedAdminIfNecessary();
    }
}
