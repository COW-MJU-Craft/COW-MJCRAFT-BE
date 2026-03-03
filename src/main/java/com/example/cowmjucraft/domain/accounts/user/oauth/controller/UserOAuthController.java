package com.example.cowmjucraft.domain.accounts.user.oauth.controller;

import com.example.cowmjucraft.domain.accounts.auth.dto.request.RefreshTokenRequestDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.dto.request.KakaoLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.dto.request.NaverLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.dto.response.UserSocialLoginResponseDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.service.UserOAuthService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/oauth")
public class UserOAuthController implements UserOAuthControllerDocs {

    private final UserOAuthService userOAuthService;

    @Override
    @PostMapping("/naver/login")
    public ResponseEntity<ApiResult<UserSocialLoginResponseDto>> loginWithNaver(@Valid @RequestBody NaverLoginRequestDto request) {
        return ApiResponse.of(SuccessType.SUCCESS, userOAuthService.loginWithNaver(request));
    }

    @Override
    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResult<UserSocialLoginResponseDto>> loginWithKakao(@Valid @RequestBody KakaoLoginRequestDto request) {
        return ApiResponse.of(SuccessType.SUCCESS, userOAuthService.loginWithKakao(request));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<UserSocialLoginResponseDto>> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        return ApiResponse.of(SuccessType.SUCCESS, userOAuthService.refresh(request.refreshToken()));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<ApiResult<Void>> logout(@AuthenticationPrincipal String memberId) {
        userOAuthService.logout(memberId);
        return ApiResponse.of(SuccessType.SUCCESS);
    }
}
