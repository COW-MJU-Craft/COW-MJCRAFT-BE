package com.example.cowmjucraft.domain.accounts.admin.account.controller;

import com.example.cowmjucraft.domain.accounts.admin.account.dto.request.AdminAccountUpdateRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.login.dto.response.AdminLoginResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin Account", description = "관리자 계정 수정 API")
public interface AdminAccountControllerDocs {

    @Operation(
            summary = "관리자 아이디/비밀번호 변경",
            description = "현재 관리자 자격 증명을 확인한 후 userId/비밀번호를 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공", content = @Content(schema = @Schema(implementation = AdminLoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 자격 증명"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 아님"),
            @ApiResponse(responseCode = "409", description = "userId 중복")
    })
    AdminLoginResponseDto updateAdminAccount(@Valid @RequestBody AdminAccountUpdateRequestDto request);
}
