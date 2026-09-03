package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.OrderPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPolicyRepository extends JpaRepository<OrderPolicy, Long> {

    Optional<OrderPolicy> findFirstByOrderByIdAsc();
}
