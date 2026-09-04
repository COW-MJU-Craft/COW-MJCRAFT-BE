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

    boolean existsByRepresentativeProjectId(Long projectId);
    boolean existsByIdAndRepresentativeProjectId(Long orderId, Long projectId);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findAllByStatusOrderByCreatedAtDesc(OrderStatus status);

    @Query("""
            select o
            from Order o
            where o.representativeProject.id = :projectId
              and (:status is null or o.status = :status)
            order by o.createdAt desc
            """)
    List<Order> findAllByRepresentativeProjectIdAndStatusOrderByCreatedAtDesc(
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
