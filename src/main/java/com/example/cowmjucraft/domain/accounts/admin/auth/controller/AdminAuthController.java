package com.example.cowmjucraft.domain.accounts.admin.auth.controller;

import com.example.cowmjucraft.domain.accounts.auth.dto.request.RefreshTokenRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.auth.dto.request.AdminLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.auth.dto.response.AdminLoginTokenResponseDto;
import com.example.cowmjucraft.domain.accounts.admin.auth.service.AdminAuthService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController implements AdminAuthControllerDocs {

    private final AdminAuthService adminAuthService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<ApiResult<AdminLoginTokenResponseDto>> login(@Valid @RequestBody AdminLoginRequestDto request) {
        AdminAuthService.LoginResult loginResult = adminAuthService.login(request);
        AdminLoginTokenResponseDto response = AdminLoginTokenResponseDto.from(loginResult.admin(), loginResult.tokenPair());
        return ApiResponse.of(SuccessType.SUCCESS, response);
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<AdminLoginTokenResponseDto>> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        AdminAuthService.LoginResult refreshResult = adminAuthService.refresh(request.refreshToken());
        AdminLoginTokenResponseDto response = AdminLoginTokenResponseDto.from(refreshResult.admin(), refreshResult.tokenPair());
        return ApiResponse.of(SuccessType.SUCCESS, response);
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<ApiResult<Void>> logout(@AuthenticationPrincipal String loginId) {
        adminAuthService.logout(loginId);
        return ApiResponse.of(SuccessType.SUCCESS);
    }
}
