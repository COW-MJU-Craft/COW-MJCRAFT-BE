package com.example.cowmjucraft.global.seed;

import com.example.cowmjucraft.domain.accounts.admin.repository.AdminRepository;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * 시딩이 실제 트랜잭션 안에서 실행되는지 검증한다.
 *
 * <p>과거에는 {@code AdminInitializer}가 자기 자신의 {@code @Transactional} 메서드를
 * 호출해 proxy를 우회했고, 그 결과 exists 검사와 save가 각각 별도 트랜잭션으로 동작했다.
 * 이 테스트는 Bean 분리 이후 트랜잭션 경계가 의도대로 잡히는지를 확인한다.
 */
@SpringBootTest
class AdminSeederTransactionTest {

    @MockitoBean
    private AdminRepository adminRepository;

    @Autowired
    private AdminSeeder adminSeeder;

    @Test
    void seedAdminIfNecessary_proxy를거쳐_실제트랜잭션안에서실행된다() {
        // given
        AtomicBoolean transactionActive = new AtomicBoolean(false);
        given(adminRepository.existsByLoginId(anyString())).willAnswer(invocation -> {
            transactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
            return true;
        });

        // when
        adminSeeder.seedAdminIfNecessary();

        // then
        assertThat(transactionActive).isTrue();
    }
}
