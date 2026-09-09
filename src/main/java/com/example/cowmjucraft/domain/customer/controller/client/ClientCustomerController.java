package com.example.cowmjucraft.domain.customer.controller.client;

import com.example.cowmjucraft.domain.customer.dto.request.CustomerCredentialRequestDto;
import com.example.cowmjucraft.domain.customer.dto.request.CustomerEmailCodeRequestDto;
import com.example.cowmjucraft.domain.customer.dto.request.CustomerEnrollRequestDto;
import com.example.cowmjucraft.domain.customer.dto.request.CustomerProfileSaveRequestDto;
import com.example.cowmjucraft.domain.customer.dto.response.CustomerEnrollResponseDto;
import com.example.cowmjucraft.domain.customer.dto.response.CustomerOrderListItemResponseDto;
import com.example.cowmjucraft.domain.customer.dto.response.CustomerPrefillResponseDto;
import com.example.cowmjucraft.domain.customer.service.CustomerAccountService;
import com.example.cowmjucraft.domain.customer.service.CustomerEmailCodeService;
import com.example.cowmjucraft.domain.customer.service.CustomerOrderQueryService;
import com.example.cowmjucraft.domain.customer.service.CustomerPrefillService;
import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 자격증명은 항상 요청 body로 받는다. 쿼리 스트링에 이메일·비밀번호를 남기지 않기
 * 위해 조회성 엔드포인트도 POST를 쓴다.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customers")
public class ClientCustomerController implements ClientCustomerControllerDocs {

    private final CustomerEmailCodeService customerEmailCodeService;
    private final CustomerAccountService customerAccountService;
    private final CustomerPrefillService customerPrefillService;
    private final CustomerOrderQueryService customerOrderQueryService;

    @PostMapping("/email-code")
    @Override
    public ResponseEntity<ApiResult<Void>> sendEmailCode(
            @Valid @RequestBody CustomerEmailCodeRequestDto request,
            HttpServletRequest httpRequest
    ) {
        customerEmailCodeService.issue(request.email(), httpRequest.getRemoteAddr());
        return ApiResponse.of(SuccessType.ACCEPTED);
    }

    @PostMapping("/enroll")
    @Override
    public ResponseEntity<ApiResult<CustomerEnrollResponseDto>> enroll(
            @Valid @RequestBody CustomerEnrollRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, customerAccountService.enroll(request));
    }

    @PostMapping("/prefill")
    @Override
    public ResponseEntity<ApiResult<CustomerPrefillResponseDto>> prefill(
            @Valid @RequestBody CustomerCredentialRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, customerPrefillService.prefill(request));
    }

    @PostMapping("/profile")
    @Override
    public ResponseEntity<ApiResult<CustomerEnrollResponseDto>> saveProfile(
            @Valid @RequestBody CustomerProfileSaveRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, customerAccountService.saveProfile(request));
    }

    @PostMapping("/orders")
    @Override
    public ResponseEntity<ApiResult<List<CustomerOrderListItemResponseDto>>> getOrders(
            @Valid @RequestBody CustomerCredentialRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, customerOrderQueryService.getOrders(request));
    }

    @PostMapping("/orders/{orderId}")
    @Override
    public ResponseEntity<ApiResult<OrderDetailResponseDto>> getOrderDetail(
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody CustomerCredentialRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, customerOrderQueryService.getOrderDetail(orderId, request));
    }
}
