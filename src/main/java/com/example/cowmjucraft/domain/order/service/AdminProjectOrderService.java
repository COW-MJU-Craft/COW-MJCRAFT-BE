package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.response.AdminOrderBulkAdvanceResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderListItemResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderStatusResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminProjectOrderStatisticsResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderBuyer;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderBuyerRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import com.example.cowmjucraft.domain.order.repository.ProjectOrderStatisticsProjection;
import com.example.cowmjucraft.domain.project.exception.ProjectErrorType;
import com.example.cowmjucraft.domain.project.exception.ProjectException;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProjectOrderService {

    private static final Set<OrderStatus> STATISTICS_INCLUDED_STATUSES = EnumSet.of(
            OrderStatus.PAID,
            OrderStatus.IN_PRODUCTION,
            OrderStatus.READY_TO_SHIP,
            OrderStatus.DELIVERED,
            OrderStatus.REFUND_REQUESTED
    );

    private final ProjectRepository projectRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderBuyerRepository orderBuyerRepository;
    private final AdminOrderPaymentService adminOrderPaymentService;

    @Transactional(readOnly = true)
    public List<AdminOrderListItemResponseDto> getOrders(Long projectId, OrderStatus status) {
        validateProjectExists(projectId);
        List<Order> orders = orderRepository.findAllByProjectIdAndStatusOrderByCreatedAtDesc(projectId, status);
        return toListResponses(orders);
    }

    @Transactional(readOnly = true)
    public AdminProjectOrderStatisticsResponseDto getStatistics(Long projectId) {
        validateProjectExists(projectId);
        ProjectOrderStatisticsProjection statistics = orderItemRepository.calculateProjectOrderStatistics(
                projectId,
                STATISTICS_INCLUDED_STATUSES
        );
        long totalOrderAmount = statistics.getTotalOrderAmount() == null
                ? 0L
                : statistics.getTotalOrderAmount();
        return new AdminProjectOrderStatisticsResponseDto(statistics.getOrderCount(), totalOrderAmount);
    }

    @Transactional
    public AdminOrderStatusResponseDto advanceOrderStatus(Long projectId, Long orderId) {
        AdminOrderBulkAdvanceResponseDto result = advanceOrderStatuses(projectId, List.of(orderId));
        return new AdminOrderStatusResponseDto(orderId, result.newStatus());
    }

    @Transactional
    public AdminOrderBulkAdvanceResponseDto advanceOrderStatuses(Long projectId, Collection<Long> requestedOrderIds) {
        validateProjectExists(projectId);
        List<Long> orderIds = normalizeOrderIds(requestedOrderIds);
        List<Order> orders = orderRepository.findAllByIdInForUpdate(orderIds);

        if (orders.size() != orderIds.size()) {
            throw new OrderException(OrderErrorType.ORDER_NOT_FOUND, "orderIds=" + orderIds);
        }

        validateOrdersBelongToProject(projectId, orderIds);
        OrderStatus currentStatus = orders.getFirst().getStatus();
        boolean sameStatus = orders.stream().allMatch(order -> order.getStatus() == currentStatus);
        if (!sameStatus) {
            throw new OrderException(OrderErrorType.ORDER_STATUS_MISMATCH, "orderIds=" + orderIds);
        }

        OrderStatus nextStatus = OrderStatusTransitionPolicy.nextNormalStatus(currentStatus);
        OrderStatusTransitionPolicy.validate(currentStatus, nextStatus);

        if (currentStatus == OrderStatus.PENDING_DEPOSIT) {
            for (Order order : orders) {
                adminOrderPaymentService.confirmPaid(order.getId());
            }
        } else {
            orders.forEach(order -> order.updateStatus(nextStatus));
        }

        return new AdminOrderBulkAdvanceResponseDto(
                currentStatus.name(),
                nextStatus.name(),
                orderIds
        );
    }

    private List<AdminOrderListItemResponseDto> toListResponses(List<Order> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, OrderBuyer> buyerByOrderId = orderBuyerRepository.findAllByOrderIdIn(orderIds).stream()
                .collect(Collectors.toMap(OrderBuyer::getOrderId, Function.identity()));

        return orders.stream()
                .map(order -> {
                    OrderBuyer buyer = buyerByOrderId.get(order.getId());
                    return new AdminOrderListItemResponseDto(
                            order.getId(),
                            order.getOrderNo(),
                            order.getStatus().name(),
                            order.getFinalAmount(),
                            order.getDepositorName(),
                            buyer == null ? null : buyer.getName(),
                            buyer == null ? null : buyer.getPhone(),
                            order.getCreatedAt(),
                            order.getDepositDeadline()
                    );
                })
                .toList();
    }

    private List<Long> normalizeOrderIds(Collection<Long> requestedOrderIds) {
        if (requestedOrderIds == null || requestedOrderIds.isEmpty()) {
            throw new OrderException(OrderErrorType.ORDER_IDS_REQUIRED);
        }
        return requestedOrderIds.stream()
                .distinct()
                .sorted()
                .toList();
    }

    private void validateOrdersBelongToProject(Long projectId, List<Long> orderIds) {
        Set<Long> projectOrderIds = new HashSet<>(
                orderItemRepository.findOrderIdsByProjectIdAndOrderIdIn(projectId, orderIds)
        );
        if (!projectOrderIds.containsAll(orderIds)) {
            throw new OrderException(
                    OrderErrorType.ORDER_NOT_FOUND,
                    "projectId=" + projectId + ", orderIds=" + orderIds
            );
        }
    }

    private void validateProjectExists(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectException(ProjectErrorType.PROJECT_NOT_FOUND, "projectId=" + projectId);
        }
    }

}
