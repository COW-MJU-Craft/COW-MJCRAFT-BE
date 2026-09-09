package com.example.cowmjucraft.domain.customer.repository;

import com.example.cowmjucraft.domain.customer.entity.CustomerEmailCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerEmailCodeRepository extends JpaRepository<CustomerEmailCode, Long> {

    /** 검증 대상은 항상 가장 최근 코드 하나다. 이전 코드는 발급 시점에 무효화된다. */
    Optional<CustomerEmailCode> findFirstByEmailOrderByIdDesc(String email);

    /** 재발급 간격(60초) 확인용. */
    int countByEmailAndCreatedAtAfter(String email, LocalDateTime after);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CustomerEmailCode c
               set c.consumedAt = :now
             where c.email = :email
               and c.consumedAt is null
            """)
    int consumeAllByEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    @Modifying
    @Query("delete from CustomerEmailCode c where c.expiresAt < :threshold")
    int deleteAllByExpiresAtBefore(@Param("threshold") LocalDateTime threshold);
}
