package com.example.cowmjucraft.domain.order.controller.client;

import com.example.cowmjucraft.domain.order.dto.request.OrderCreateRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.OrderLookupRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderCreateResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderLookupIdAvailabilityResponseDto;
import com.example.cowmjucraft.domain.order.service.OrderCreateService;
import com.example.cowmjucraft.domain.order.service.OrderDetailQueryService;
import com.example.cowmjucraft.domain.order.service.OrderLookupIdService;
import com.example.cowmjucraft.domain.order.service.OrderQueryByTokenService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ClientOrderController implements ClientOrderControllerDocs {

    private final OrderCreateService orderCreateService;
    private final OrderLookupIdService orderLookupIdService;
    private final OrderDetailQueryService orderDetailQueryService;
    private final OrderQueryByTokenService orderQueryByTokenService;

    @PostMapping("/orders")
    @Override
    public ApiResult<OrderCreateResponseDto> createOrder(
            @Valid @RequestBody OrderCreateRequestDto request
    ) {
        return ApiResult.success(SuccessType.CREATED, orderCreateService.createOrder(request));
    }

    @GetMapping("/orders/lookup-id/availability")
    @Override
    public ApiResult<OrderLookupIdAvailabilityResponseDto> checkLookupIdAvailability(
            @RequestParam("lookupId") String lookupId
    ) {
        return ApiResult.success(SuccessType.SUCCESS, orderLookupIdService.checkAvailability(lookupId));
    }

    @PostMapping("/orders/lookup")
    @Override
    public ApiResult<OrderDetailResponseDto> lookupOrder(
            @Valid @RequestBody OrderLookupRequestDto request
    ) {
        return ApiResult.success(
                SuccessType.SUCCESS,
                orderDetailQueryService.getByLookupIdAndPassword(request.lookupId(), request.password())
        );
    }

    @GetMapping("/orders/view")
    @Override
    public ApiResult<OrderDetailResponseDto> viewOrderByToken(@RequestParam("token") String token) {
        return ApiResult.success(SuccessType.SUCCESS, orderQueryByTokenService.getOrderDetailByToken(token));
    }
}
