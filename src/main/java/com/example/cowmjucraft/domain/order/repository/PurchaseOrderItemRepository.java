package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
}
