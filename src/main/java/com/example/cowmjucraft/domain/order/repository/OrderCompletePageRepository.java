package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.OrderCompletePage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderCompletePageRepository extends JpaRepository<OrderCompletePage, Long> {

    Optional<OrderCompletePage> findFirstByOrderByIdAsc();
}