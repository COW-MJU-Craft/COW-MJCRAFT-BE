package com.example.cowmjucraft.domain.order.controller.admin;

import com.example.cowmjucraft.domain.order.dto.request.AdminOrderCancelRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderListItemResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderStatusResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.service.AdminOrderPaymentService;
import com.example.cowmjucraft.domain.order.service.AdminOrderQueryService;
import com.example.cowmjucraft.domain.order.service.AdminOrderRefundService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminOrderController implements AdminOrderControllerDocs {

    private final AdminOrderQueryService adminOrderQueryService;
    private final AdminOrderPaymentService adminOrderPaymentService;
    private final AdminOrderRefundService adminOrderRefundService;

    @GetMapping("/orders")
    @Override
    public ApiResult<List<AdminOrderListItemResponseDto>> getOrders(
            @RequestParam(value = "status", required = false) OrderStatus status
    ) {
        return ApiResult.success(SuccessType.SUCCESS, adminOrderQueryService.getOrders(status));
    }

    @GetMapping("/orders/{orderId}")
    @Override
    public ApiResult<OrderDetailResponseDto> getOrderDetail(@PathVariable Long orderId) {
        return ApiResult.success(SuccessType.SUCCESS, adminOrderQueryService.getOrderDetail(orderId));
    }

    @PostMapping("/orders/{orderId}/confirm-paid")
    @Override
    public ApiResult<AdminOrderStatusResponseDto> confirmPaid(@PathVariable Long orderId) {
        return ApiResult.success(SuccessType.SUCCESS, adminOrderPaymentService.confirmPaid(orderId));
    }

    @PostMapping("/orders/{orderId}/cancel")
    @Override
    public ApiResult<AdminOrderStatusResponseDto> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) AdminOrderCancelRequestDto request
    ) {
        String reason = request == null ? null : request.reason();
        return ApiResult.success(SuccessType.SUCCESS, adminOrderQueryService.cancelOrder(orderId, reason));
    }

    @PostMapping("/orders/{orderId}/confirm-refund")
    @Override
    public ApiResult<AdminOrderStatusResponseDto> confirmRefund(@PathVariable Long orderId) {
        return ApiResult.success(SuccessType.SUCCESS, adminOrderRefundService.confirmRefund(orderId));
    }
}
