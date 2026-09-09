package com.example.cowmjucraft.domain.order.controller.client;

import com.example.cowmjucraft.domain.order.dto.request.OrderCreateRequestDto;
import com.example.cowmjucraft.domain.order.dto.request.OrderLookupRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderCompletePageResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderCreateResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderLookupIdAvailabilityResponseDto;
import com.example.cowmjucraft.domain.order.exception.OrderErrorType;
import com.example.cowmjucraft.domain.order.exception.OrderException;
import com.example.cowmjucraft.domain.order.service.OrderCompletePageService;
import com.example.cowmjucraft.domain.order.service.OrderCreateService;
import com.example.cowmjucraft.domain.order.service.OrderDetailQueryService;
import com.example.cowmjucraft.domain.order.service.OrderLookupIdService;
import com.example.cowmjucraft.domain.order.service.OrderQueryByTokenService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ClientOrderController implements ClientOrderControllerDocs {

    static final String ORDER_VIEW_TOKEN_HEADER = "X-Order-View-Token";

    private final OrderCreateService orderCreateService;
    private final OrderLookupIdService orderLookupIdService;
    private final OrderDetailQueryService orderDetailQueryService;
    private final OrderQueryByTokenService orderQueryByTokenService;
    private final OrderCompletePageService orderCompletePageService;

    @PostMapping("/orders")
    @Override
    public ResponseEntity<ApiResult<OrderCreateResponseDto>> createOrder(
            @Valid @RequestBody OrderCreateRequestDto request
    ) {
        return ApiResponse.of(SuccessType.CREATED, orderCreateService.createOrder(request));
    }

    @GetMapping("/orders/lookup-id/availability")
    @Override
    public ResponseEntity<ApiResult<OrderLookupIdAvailabilityResponseDto>> checkLookupIdAvailability(
            @RequestParam("lookupId") String lookupId
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, orderLookupIdService.checkAvailability(lookupId));
    }

    @PostMapping("/orders/lookup")
    @Override
    public ResponseEntity<ApiResult<OrderDetailResponseDto>> lookupOrder(
            @Valid @RequestBody OrderLookupRequestDto request
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                orderDetailQueryService.getByLookupIdAndPassword(request.lookupId(), request.password())
        );
    }

    @GetMapping("/orders/view")
    @Override
    public ResponseEntity<ApiResult<OrderDetailResponseDto>> viewOrderByToken(
            @RequestHeader(name = ORDER_VIEW_TOKEN_HEADER, required = false) String headerToken,
            @RequestParam(name = "token", required = false) String queryToken
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                orderQueryByTokenService.getOrderDetailByToken(resolveViewToken(headerToken, queryToken))
        );
    }

    @GetMapping("/orders/complete-page")
    @Override
    public ResponseEntity<ApiResult<OrderCompletePageResponseDto>> getOrderCompletePage(
            @RequestHeader(name = ORDER_VIEW_TOKEN_HEADER, required = false) String headerToken,
            @RequestParam(name = "token", required = false) String queryToken
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                orderCompletePageService.getOrderCompletePage(resolveViewToken(headerToken, queryToken))
        );
    }

    /**
     * 조회 토큰은 헤더를 우선한다. 쿼리 파라미터는 액세스 로그·프록시 로그·Referer에 남으므로
     * 헤더 전달을 권장하되, 기존 링크와 프런트엔드 호환을 위해 당분간 함께 받는다.
     */
    private String resolveViewToken(String headerToken, String queryToken) {
        if (headerToken != null && !headerToken.isBlank()) {
            return headerToken;
        }
        if (queryToken != null && !queryToken.isBlank()) {
            return queryToken;
        }
        throw new OrderException(OrderErrorType.VIEW_TOKEN_REQUIRED);
    }
}
