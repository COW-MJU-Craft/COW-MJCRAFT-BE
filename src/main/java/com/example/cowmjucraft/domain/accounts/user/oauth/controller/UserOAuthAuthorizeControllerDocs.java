package com.example.cowmjucraft.domain.accounts.user.oauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User Social Auth", description = "네이버/카카오 간편 로그인 API")
public interface UserOAuthAuthorizeControllerDocs {

    @Operation(
            summary = "카카오 OAuth 인가 페이지 리다이렉트",
            description = "카카오 OAuth authorize URL로 302 리다이렉트합니다. Location 헤더에 인가 URL이 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "카카오 authorize URL로 리다이렉트"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> authorizeKakao();

    @Operation(
            summary = "네이버 OAuth 인가 페이지 리다이렉트",
            description = "네이버 OAuth authorize URL로 302 리다이렉트합니다. Location 헤더에 인가 URL이 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "네이버 authorize URL로 리다이렉트"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> authorizeNaver();
}
