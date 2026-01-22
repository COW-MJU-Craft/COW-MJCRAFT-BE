package com.example.cowmjucraft.domain.accounts.user.oauth.controller;

import com.example.cowmjucraft.domain.accounts.user.oauth.dto.request.KakaoLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.dto.request.NaverLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.dto.response.UserSocialLoginResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User Social Auth", description = "네이버/카카오 간편 로그인 API")
public interface UserOAuthControllerDocs {

    @Operation(
            summary = "네이버 간편 로그인",
            description = "네이버 OAuth 인가 코드로 로그인 후 JWT를 응답 바디에 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = UserSocialLoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 인가 코드")
    })
    ResponseEntity<UserSocialLoginResponseDto> loginWithNaver(@Valid @RequestBody NaverLoginRequestDto request);

    @Operation(
            summary = "카카오 간편 로그인",
            description = "카카오 OAuth 인가 코드로 로그인 후 JWT를 응답 바디에 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = UserSocialLoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 인가 코드")
    })
    ResponseEntity<UserSocialLoginResponseDto> loginWithKakao(@Valid @RequestBody KakaoLoginRequestDto request);
}
