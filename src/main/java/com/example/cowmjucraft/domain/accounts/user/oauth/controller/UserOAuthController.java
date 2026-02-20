package com.example.cowmjucraft.domain.accounts.user.oauth.controller;

import com.example.cowmjucraft.domain.accounts.user.oauth.dto.request.KakaoLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.dto.request.NaverLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.dto.response.UserSocialLoginResponseDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.service.UserOAuthService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
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
    public ApiResult<UserSocialLoginResponseDto> loginWithNaver(@Valid @RequestBody NaverLoginRequestDto request) {
        return ApiResult.success(SuccessType.SUCCESS, userOAuthService.loginWithNaver(request));
    }

    @Override
    @PostMapping("/kakao/login")
    public ApiResult<UserSocialLoginResponseDto> loginWithKakao(@Valid @RequestBody KakaoLoginRequestDto request) {
        return ApiResult.success(SuccessType.SUCCESS, userOAuthService.loginWithKakao(request));
    }
}
