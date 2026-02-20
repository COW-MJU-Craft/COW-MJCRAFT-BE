package com.example.cowmjucraft.domain.mypage.controller;

import com.example.cowmjucraft.domain.mypage.dto.request.MyPageAddressRequestDto;
import com.example.cowmjucraft.domain.mypage.dto.response.MyPageAddressResponseDto;
import com.example.cowmjucraft.domain.mypage.dto.response.MyPageResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "MyPage", description = "마이페이지 API")
public interface MyPageControllerDocs {

    @Operation(summary = "마이페이지 조회", description = "현재 로그인 사용자 정보 및 배송지 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<ApiResult<MyPageResponseDto>> getMyPage(
            @Parameter(hidden = true)
            String memberId
    );

    @Operation(summary = "배송지 등록", description = "배송지가 없을 때 등록합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "409", description = "이미 배송지 존재")
    })
    ResponseEntity<ApiResult<MyPageAddressResponseDto>> createAddress(
            @Parameter(hidden = true)
            String memberId,
            @Valid @RequestBody MyPageAddressRequestDto request
    );

    @Operation(summary = "배송지 수정", description = "등록된 배송지 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "배송지 없음")
    })
    ResponseEntity<ApiResult<MyPageAddressResponseDto>> updateAddress(
            @Parameter(hidden = true)
            String memberId,
            @Valid @RequestBody MyPageAddressRequestDto request
    );

    @Operation(summary = "배송지 삭제", description = "등록된 배송지를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "배송지 없음")
    })
    ResponseEntity<ApiResult<Void>> deleteAddress(
            @Parameter(hidden = true)
            String memberId
    );
}
