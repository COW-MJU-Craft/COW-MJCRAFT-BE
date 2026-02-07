package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.OrderViewToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderViewTokenRepository extends JpaRepository<OrderViewToken, Long> {

    Optional<OrderViewToken> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update OrderViewToken t
               set t.revokedAt = :revokedAt
             where t.order.id = :orderId
               and t.revokedAt is null
            """)
    int revokeActiveTokens(@Param("orderId") Long orderId, @Param("revokedAt") LocalDateTime revokedAt);
}
