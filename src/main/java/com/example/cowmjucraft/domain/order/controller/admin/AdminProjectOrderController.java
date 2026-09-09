package com.example.cowmjucraft.domain.order.controller.admin;

import com.example.cowmjucraft.domain.order.dto.request.AdminOrderBulkAdvanceRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderBulkAdvanceResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderListItemResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderStatusResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminProjectOrderStatisticsResponseDto;
import com.example.cowmjucraft.domain.order.entity.OrderStatus;
import com.example.cowmjucraft.domain.order.service.AdminProjectOrderService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/projects/{projectId}/orders")
public class AdminProjectOrderController implements AdminProjectOrderControllerDocs {

    private final AdminProjectOrderService adminProjectOrderService;

    @GetMapping
    @Override
    public ResponseEntity<ApiResult<List<AdminOrderListItemResponseDto>>> getOrders(
            @PathVariable Long projectId,
            @RequestParam(value = "status", required = false) OrderStatus status
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminProjectOrderService.getOrders(projectId, status));
    }

    @GetMapping("/statistics")
    @Override
    public ResponseEntity<ApiResult<AdminProjectOrderStatisticsResponseDto>> getStatistics(
            @PathVariable Long projectId
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, adminProjectOrderService.getStatistics(projectId));
    }

    @PostMapping("/{orderId}/advance-status")
    @Override
    public ResponseEntity<ApiResult<AdminOrderStatusResponseDto>> advanceOrderStatus(
            @PathVariable Long projectId,
            @PathVariable Long orderId
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                adminProjectOrderService.advanceOrderStatus(projectId, orderId)
        );
    }

    @PostMapping("/advance-status")
    @Override
    public ResponseEntity<ApiResult<AdminOrderBulkAdvanceResponseDto>> advanceOrderStatuses(
            @PathVariable Long projectId,
            @Valid @RequestBody AdminOrderBulkAdvanceRequestDto request
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                adminProjectOrderService.advanceOrderStatuses(projectId, request.orderIds())
        );
    }

}
