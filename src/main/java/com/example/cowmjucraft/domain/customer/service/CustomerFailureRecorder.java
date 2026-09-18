package com.example.cowmjucraft.domain.customer.service;

import com.example.cowmjucraft.domain.customer.repository.CustomerEmailCodeRepository;
import com.example.cowmjucraft.domain.customer.repository.CustomerRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 실패 횟수를 <b>별도 트랜잭션</b>에 기록한다.
 *
 * <p>이게 없으면 방어가 통째로 무력해진다. 실패를 알리는 예외는
 * {@code RuntimeException}이라 호출부 트랜잭션을 롤백시키는데, 같은 트랜잭션에서
 * 카운터를 올리면 그 증가분도 함께 사라진다. 즉 몇 번을 틀려도 카운터가 0에
 *머물러 잠금이 영영 걸리지 않는다.
 *
 * <p>{@code REQUIRES_NEW}로 호출부 트랜잭션을 잠시 멈추고 증가분만 먼저 커밋한 뒤
 * 호출부가 예외를 던지게 한다. 자기 호출(self-invocation)은 프록시를 타지 않으므로
 * 반드시 별도 빈이어야 한다.
 */
@Service
@RequiredArgsConstructor
public class CustomerFailureRecorder {

    private final CustomerRepository customerRepository;
    private final CustomerEmailCodeRepository customerEmailCodeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordPasswordFailure(Long customerId, LocalDateTime now) {
        customerRepository.findById(customerId)
                .ifPresent(customer -> customer.recordPasswordFailure(now));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordCodeFailure(Long codeId, LocalDateTime now) {
        customerEmailCodeRepository.findById(codeId)
                .ifPresent(code -> code.recordFailure(now));
    }
}
