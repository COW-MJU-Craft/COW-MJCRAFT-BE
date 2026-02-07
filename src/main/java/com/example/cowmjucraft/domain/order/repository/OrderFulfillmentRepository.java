package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.OrderFulfillment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderFulfillmentRepository extends JpaRepository<OrderFulfillment, Long> {
}
