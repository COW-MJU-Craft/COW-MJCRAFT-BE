package com.example.cowmjucraft.domain.customer.controller.client;

import com.example.cowmjucraft.domain.customer.dto.request.CustomerCredentialRequestDto;
import com.example.cowmjucraft.domain.customer.dto.request.CustomerEmailCodeRequestDto;
import com.example.cowmjucraft.domain.customer.dto.request.CustomerEnrollRequestDto;
import com.example.cowmjucraft.domain.customer.dto.request.CustomerProfileSaveRequestDto;
import com.example.cowmjucraft.domain.customer.dto.response.CustomerEnrollResponseDto;
import com.example.cowmjucraft.domain.customer.dto.response.CustomerOrderListItemResponseDto;
import com.example.cowmjucraft.domain.customer.dto.response.CustomerPrefillResponseDto;
import com.example.cowmjucraft.domain.order.dto.response.OrderDetailResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "고객 정보(사용자)", description = "이메일 + 비밀번호로 정보를 저장하고 불러오는 API. 로그인 상태(세션)를 두지 않는다.")
public interface ClientCustomerControllerDocs {

    @Operation(
            summary = "이메일 인증 코드 발송",
            description = """
                    정보 등록과 비밀번호 재설정에 공통으로 쓴다.
                    이메일 등록 여부·요청 제한 여부와 관계없이 항상 202를 반환한다 —
                    응답 차이로 계정 존재 여부가 드러나지 않게 하기 위해서다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "발송 요청 접수")
    ResponseEntity<ApiResult<Void>> sendEmailCode(CustomerEmailCodeRequestDto request, HttpServletRequest httpRequest);

    @Operation(
            summary = "정보 등록 · 비밀번호 재설정",
            description = """
                    코드로 이메일 소유를 증명하고 비밀번호를 저장한다.
                    고객이 없으면 만들고, 있으면 비밀번호를 갱신한다 — 그 갱신이 곧 재설정이다.
                    profile을 생략하면 기존 프로필은 건드리지 않는다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "저장 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "코드가 올바르지 않거나 만료됨")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "비밀번호 강도 미달")
    ResponseEntity<ApiResult<CustomerEnrollResponseDto>> enroll(CustomerEnrollRequestDto request);

    @Operation(
            summary = "저장한 정보 불러오기",
            description = """
                    주문서 자동완성용. 이름·연락처·구분·캠퍼스·학과·학번은 저장한 프로필에서,
                    환불 은행/계좌·입금자명·수령지는 최근 주문 스냅샷에서 온다.
                    자격증명을 쿼리 스트링에 노출하지 않기 위해 조회지만 POST를 쓴다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    ResponseEntity<ApiResult<CustomerPrefillResponseDto>> prefill(CustomerCredentialRequestDto request);

    @Operation(summary = "프로필 저장", description = "주문 완료 후 '변경한 정보 저장'과 '내 정보 수정'이 함께 쓴다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "저장 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    ResponseEntity<ApiResult<CustomerEnrollResponseDto>> saveProfile(CustomerProfileSaveRequestDto request);

    @Operation(
            summary = "내 주문 목록",
            description = "정보를 저장하기 전에 만든 주문도 포함된다 — 완료 메일을 그 주소가 받았기 때문이다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    ResponseEntity<ApiResult<List<CustomerOrderListItemResponseDto>>> getOrders(CustomerCredentialRequestDto request);

    @Operation(
            summary = "내 주문 상세",
            description = "남의 주문을 요청하면 403이 아니라 404를 반환한다 — 403은 주문의 존재를 흘린다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문 없음 또는 소유자 불일치")
    ResponseEntity<ApiResult<OrderDetailResponseDto>> getOrderDetail(Long orderId, CustomerCredentialRequestDto request);
}
