package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrderIdOrderByProjectItemIdAsc(Long orderId);
}
