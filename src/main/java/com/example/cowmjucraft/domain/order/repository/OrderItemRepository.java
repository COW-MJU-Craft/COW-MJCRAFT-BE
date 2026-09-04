package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrderIdOrderByProjectItemIdAsc(Long orderId);

    @Query("""
            select count(distinct oi.order.id) as orderCount,
                   coalesce(sum(oi.lineAmount), 0) as totalOrderAmount
            from OrderItem oi
            where oi.projectItem.project.id = :projectId
              and oi.order.status in :statuses
            """)
    ProjectOrderStatisticsProjection calculateProjectOrderStatistics(
            @Param("projectId") Long projectId,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    void deleteByProjectItemIdIn(List<Long> projectItemIds);
}
