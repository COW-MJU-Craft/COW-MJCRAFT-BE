package com.example.cowmjucraft.domain.customer.service;

import com.example.cowmjucraft.domain.customer.entity.Customer;
import com.example.cowmjucraft.domain.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 새 고객 행을 <b>별도 트랜잭션</b>에서 INSERT한다.
 *
 * <p>같은 이메일로 동시에 주문·등록이 들어오면 한쪽은 UNIQUE 제약에 걸린다. 이 실패가
 * 호출부(주문 생성) 트랜잭션과 같은 트랜잭션에서 나면, JPA는 flush 실패 시 트랜잭션을
 * rollback-only로 마킹한다. 그러면 같은 트랜잭션 안에서 예외를 잡아 재조회하더라도
 * 최종 커밋이 {@code UnexpectedRollbackException}으로 실패한다 — 복구가 성립하지 않는다.
 *
 * <p>{@code REQUIRES_NEW}로 INSERT를 별도 트랜잭션에 가두면 실패가 호출부 트랜잭션을
 * 오염시키지 않는다. 호출부는 깨끗한 상태로 상대가 만든 행을 재조회할 수 있다.
 * 자기 호출(self-invocation)은 프록시를 타지 않으므로 반드시 별도 빈이어야 한다.
 */
@Service
@RequiredArgsConstructor
public class CustomerCreator {

    private final CustomerRepository customerRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insert(String email) {
        customerRepository.saveAndFlush(new Customer(email));
    }
}
