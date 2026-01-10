package com.example.cowmjucraft.domain.accounts.admin.login.controller;

import com.example.cowmjucraft.domain.accounts.admin.login.dto.request.AdminLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.login.dto.response.AdminLoginResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin Login", description = "관리자 로그인 API")
public interface AdminLoginControllerDocs {

    @Operation(
            summary = "관리자 로그인",
            description = "userId/password로 로그인 후 JWT를 응답 바디에 반환합니다. Swagger Authorize에 Bearer 토큰으로 입력해 테스트하세요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = AdminLoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 자격 증명"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 아님")
    })
    ResponseEntity<AdminLoginResponseDto> login(@Valid @RequestBody AdminLoginRequestDto request);
}
