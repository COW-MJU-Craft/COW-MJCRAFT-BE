package com.example.cowmjucraft.domain.order.service;

import com.example.cowmjucraft.domain.order.dto.response.OrderCompletePageResponseDto;
import com.example.cowmjucraft.domain.order.entity.Order;
import com.example.cowmjucraft.domain.order.entity.OrderCompletePage;
import com.example.cowmjucraft.domain.order.entity.OrderItem;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.repository.OrderCompletePageRepository;
import com.example.cowmjucraft.domain.order.repository.OrderItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCompletePageService {

    private final OrderCompletePageRepository orderCompletePageRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderViewTokenService orderViewTokenService;

    @Transactional(readOnly = true)
    public OrderCompletePageResponseDto getOrderCompletePage(String rawToken) {
        Order order = orderViewTokenService.getValidOrder(rawToken);

        OrderCompletePage orderCompletePage = orderCompletePageRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new OrderException(OrderErrorType.ORDER_COMPLETE_PAGE_NOT_FOUND));

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderIdOrderByProjectItemIdAsc(order.getId());

        return new OrderCompletePageResponseDto(
                orderCompletePage.getMessageTitle(),
                orderCompletePage.getMessageDescription(),
                orderCompletePage.getPaymentInformation(),
                new OrderCompletePageResponseDto.OrderInfo(
                        order.getOrderNo(),
                        order.getStatus().name(),
                        order.getTotalAmount(),
                        order.getShippingFee(),
                        order.getFinalAmount(),
                        order.getDepositDeadline(),
                        order.getCreatedAt()
                ),
                orderItems.stream()
                        .map(orderItem -> new OrderCompletePageResponseDto.ItemInfo(
                                orderItem.getProjectItem().getId(),
                                orderItem.getItemNameSnapshot(),
                                orderItem.getQuantity(),
                                orderItem.getUnitPrice(),
                                orderItem.getLineAmount()
                        ))
                        .toList()
        );
    }
}