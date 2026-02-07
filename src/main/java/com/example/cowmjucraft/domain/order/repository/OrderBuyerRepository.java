package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderBuyerRepository extends JpaRepository<OrderBuyer, Long> {
}
