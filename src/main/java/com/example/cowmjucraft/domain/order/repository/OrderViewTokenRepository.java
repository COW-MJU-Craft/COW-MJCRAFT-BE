package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.OrderViewToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderViewTokenRepository extends JpaRepository<OrderViewToken, Long> {

    Optional<OrderViewToken> findByTokenHash(String tokenHash);
}
