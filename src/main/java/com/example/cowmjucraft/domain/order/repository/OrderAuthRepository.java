package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.OrderAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAuthRepository extends JpaRepository<OrderAuth, Long> {

    boolean existsByLookupId(String lookupId);
}
