package com.example.cowmjucraft.domain.accounts.user.oauth.controller;

import com.example.cowmjucraft.domain.accounts.auth.dto.request.RefreshTokenRequestDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.dto.request.KakaoLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.dto.request.NaverLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.dto.response.UserSocialLoginResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User Social Auth", description = "네이버/카카오 간편 로그인 API")
public interface UserOAuthControllerDocs {

    @Operation(
            summary = "네이버 간편 로그인",
            description = "네이버 OAuth 인가 코드로 로그인 후 Access/Refresh 토큰을 응답 바디에 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            schema = @Schema(implementation = UserSocialLoginResponseDto.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "memberId": 1,
                                              "userName": "홍길동",
                                              "email": "user@example.com",
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "accessTokenExpiresInSeconds": 3600,
                                              "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "refreshTokenExpiresInSeconds": 1209600
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 인가 코드")
    })
    ResponseEntity<ApiResult<UserSocialLoginResponseDto>> loginWithNaver(@Valid @RequestBody NaverLoginRequestDto request);

    @Operation(
            summary = "카카오 간편 로그인",
            description = "카카오 OAuth 인가 코드로 로그인 후 Access/Refresh 토큰을 응답 바디에 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            schema = @Schema(implementation = UserSocialLoginResponseDto.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "memberId": 1,
                                              "userName": "홍길동",
                                              "email": "user@example.com",
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "accessTokenExpiresInSeconds": 3600,
                                              "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "refreshTokenExpiresInSeconds": 1209600
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 인가 코드")
    })
    ResponseEntity<ApiResult<UserSocialLoginResponseDto>> loginWithKakao(@Valid @RequestBody KakaoLoginRequestDto request);

    @Operation(
            summary = "유저 토큰 재발급",
            description = "Refresh Token으로 새로운 Access/Refresh 토큰 쌍을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "재발급 성공",
                    content = @Content(schema = @Schema(implementation = UserSocialLoginResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰")
    })
    ResponseEntity<ApiResult<UserSocialLoginResponseDto>> refresh(@Valid @RequestBody RefreshTokenRequestDto request);

    @Operation(
            summary = "유저 로그아웃",
            description = "현재 로그인한 유저 계정의 활성 Refresh Token들을 모두 무효화합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 처리 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<ApiResult<Void>> logout(@Parameter(hidden = true) String memberId);
}
