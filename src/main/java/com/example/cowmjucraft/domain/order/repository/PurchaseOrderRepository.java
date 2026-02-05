package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
}
