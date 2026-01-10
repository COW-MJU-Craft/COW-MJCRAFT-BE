package com.example.cowmjucraft.domain.accounts.admin.auth.controller;

import com.example.cowmjucraft.domain.accounts.admin.auth.dto.request.AdminLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.auth.dto.response.AdminLoginTokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin Auth", description = "관리자 인증 API")
public interface AdminAuthControllerDocs {

    @Operation(
            summary = "관리자 로그인",
            description = "userId/password로 로그인 후 JWT를 응답 바디에 반환합니다. Swagger Authorize에 Bearer 토큰으로 입력해 테스트하세요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = AdminLoginTokenResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 자격 증명"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 아님")
    })
    ResponseEntity<AdminLoginTokenResponseDto> login(@Valid @RequestBody AdminLoginRequestDto request);

    @Operation(
            summary = "관리자 로그아웃",
            description = "서버는 JWT를 상태로 관리하지 않으며 실제 로그아웃은 클라이언트에서 토큰 삭제로 수행됩니다. " +
                    "본 API는 로그아웃 플로우 통일 및 향후 확장을 위해 제공됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 처리 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 아님")
    })
    ResponseEntity<Void> logout();
}
