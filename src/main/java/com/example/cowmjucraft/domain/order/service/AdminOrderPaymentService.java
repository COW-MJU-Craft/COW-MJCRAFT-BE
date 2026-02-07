package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import com.example.cowmjucraft.domain.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminOrderPaymentService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProjectItemRepository projectItemRepository;

    @Transactional
    public void confirmPaid(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "주문을 찾을 수 없습니다. (orderId=" + orderId + ")"
                ));

        if (order.getStatus() != OrderStatus.PENDING_DEPOSIT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "입금 대기 상태의 주문만 결제 확정할 수 있습니다. (orderId=" + orderId + ")"
            );
        }

        if (order.getStockDeductedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 재고 차감이 완료된 주문입니다. 중복 결제 확정을 진행할 수 없습니다. (orderId=" + orderId + ")"
            );
        }

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderIdOrderByProjectItemIdAsc(orderId);
        if (orderItems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "주문 항목이 없어 결제 확정을 진행할 수 없습니다. (orderId=" + orderId + ")"
            );
        }

        for (OrderItem orderItem : orderItems) {
            ProjectItem projectItem = projectItemRepository.findByIdForUpdate(orderItem.getProjectItem().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "주문 항목의 상품을 찾을 수 없습니다. (projectItemId="
                                    + orderItem.getProjectItem().getId() + ")"
                    ));

            if (projectItem.getSaleType() != ItemSaleType.NORMAL) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "NORMAL 상품만 결제 확정 시 재고 차감이 가능합니다. (projectItemId=" + projectItem.getId() + ")"
                );
            }

            Integer stockQty = projectItem.getStockQty();
            int orderQty = orderItem.getQuantity();

            if (stockQty == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "상품 재고 정보가 없어 결제 확정을 진행할 수 없습니다. (projectItemId=" + projectItem.getId() + ")"
                );
            }
            if (stockQty < orderQty) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "재고가 부족하여 결제 확정을 진행할 수 없습니다. (projectItemId=" + projectItem.getId() + ")"
                );
            }

            projectItem.updateStockQty(stockQty - orderQty);
        }

        LocalDateTime now = LocalDateTime.now();

        order.updateStatus(OrderStatus.PAID);
        order.updatePaidAt(now);
        order.updateStockDeductedAt(now);
    }
}
