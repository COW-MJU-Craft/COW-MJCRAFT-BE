package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.OrderItemOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemOptionRepository extends JpaRepository<OrderItemOption, Long> {

    List<OrderItemOption> findByOrderItemIdIn(List<Long> orderItemIds);

    boolean existsByOptionValueId(Long optionValueId);

    boolean existsByOptionValueIdIn(List<Long> optionValueIds);
}
