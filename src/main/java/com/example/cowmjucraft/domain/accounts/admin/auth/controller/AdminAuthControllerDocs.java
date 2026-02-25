package com.example.cowmjucraft.domain.accounts.admin.auth.controller;

import com.example.cowmjucraft.domain.accounts.admin.auth.dto.request.AdminLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.auth.dto.response.AdminLoginTokenResponseDto;
import com.example.cowmjucraft.domain.accounts.auth.dto.request.RefreshTokenRequestDto;
import com.example.cowmjucraft.global.response.ApiResult;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth - Admin", description = "관리자 인증 API")
public interface AdminAuthControllerDocs {

    @Operation(
            summary = "관리자 로그인",
            description = "userId/password로 로그인 후 Access/Refresh 토큰을 응답 바디에 반환합니다. Swagger Authorize에는 accessToken을 사용하세요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = AdminLoginTokenResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 자격 증명"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 아님")
    })
    ResponseEntity<ApiResult<AdminLoginTokenResponseDto>> login(@Valid @RequestBody AdminLoginRequestDto request);

    @Operation(
            summary = "관리자 토큰 재발급",
            description = "Refresh Token으로 새로운 Access/Refresh 토큰 쌍을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공", content = @Content(schema = @Schema(implementation = AdminLoginTokenResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰")
    })
    ResponseEntity<ApiResult<AdminLoginTokenResponseDto>> refresh(@Valid @RequestBody RefreshTokenRequestDto request);

    @Operation(
            summary = "관리자 로그아웃",
            description = "현재 로그인한 관리자 계정의 활성 Refresh Token들을 모두 무효화합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 처리 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 아님")
    })
    ResponseEntity<ApiResult<Void>> logout(@Parameter(hidden = true) String loginId);
}
