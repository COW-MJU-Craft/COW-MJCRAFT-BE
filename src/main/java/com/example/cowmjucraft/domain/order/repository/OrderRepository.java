package com.example.cowmjucraft.domain.order.repository;

import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderFulfillmentMethod;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByRepresentativeProjectId(Long projectId);
    boolean existsByIdAndRepresentativeProjectId(Long orderId, Long projectId);

    /** 고객의 주문 목록. 최신 주문이 먼저 온다. */
    List<Order> findAllByCustomerIdOrderByCreatedAtDescIdDesc(Long customerId);

    /** 불러오기가 참조하는 "가장 최근 주문". */
    Optional<Order> findFirstByCustomerIdOrderByCreatedAtDescIdDesc(Long customerId);

    /** 소유권 검증을 포함한 단건 조회. 남의 주문이면 비어 있는 결과가 온다. */
    Optional<Order> findByIdAndCustomerId(Long orderId, Long customerId);

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

    @Query("""
            select o
            from Order o
            where (:projectId is null or o.representativeProject.id = :projectId)
              and (:startAt is null or o.createdAt >= :startAt)
              and (:endAtExclusive is null or o.createdAt < :endAtExclusive)
              and (:status is null or o.status = :status)
              and (:fulfillmentMethod is null or exists (
                    select f.orderId
                    from OrderFulfillment f
                    where f.order = o
                      and f.method = :fulfillmentMethod
              ))
            order by o.createdAt desc, o.id desc
            """)
    List<Order> findAllForExport(
            @Param("projectId") Long projectId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAtExclusive") LocalDateTime endAtExclusive,
            @Param("status") OrderStatus status,
            @Param("fulfillmentMethod") OrderFulfillmentMethod fulfillmentMethod
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :orderId")
    Optional<Order> findByIdForUpdate(@Param("orderId") Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id in :orderIds order by o.id asc")
    List<Order> findAllByIdInForUpdate(@Param("orderIds") List<Long> orderIds);
}
