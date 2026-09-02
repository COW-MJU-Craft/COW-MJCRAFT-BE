package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderNo(String orderNo);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findAllByStatusOrderByCreatedAtDesc(OrderStatus status);

    @Query("""
            select distinct o
            from Order o
            join OrderItem oi on oi.order = o
            where oi.projectItem.project.id = :projectId
              and (:status is null or o.status = :status)
            order by o.createdAt desc
            """)
    List<Order> findAllByProjectIdAndStatusOrderByCreatedAtDesc(
            @Param("projectId") Long projectId,
            @Param("status") OrderStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :orderId")
    Optional<Order> findByIdForUpdate(@Param("orderId") Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id in :orderIds order by o.id asc")
    List<Order> findAllByIdInForUpdate(@Param("orderIds") List<Long> orderIds);
}
