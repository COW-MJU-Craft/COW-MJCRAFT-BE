package com.example.cowmjucraft.domain.accounts.admin.login.controller;

import com.example.cowmjucraft.domain.accounts.admin.login.dto.request.AdminLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.login.dto.response.AdminLoginResponseDto;
import com.example.cowmjucraft.domain.accounts.admin.login.dto.response.AdminLoginTokenResponseDto;
import com.example.cowmjucraft.domain.accounts.admin.login.service.AdminLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminLoginController implements AdminLoginControllerDocs {

    private final AdminLoginService adminLoginService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<AdminLoginTokenResponseDto> login(@RequestBody AdminLoginRequestDto request) {
        // TODO: 관리자 로그인 엔드포인트에 rate limit 적용 필요 (무차별 대입 방지 목적).
        AdminLoginService.LoginResult loginResult = adminLoginService.login(request);

        ResponseCookie cookie = ResponseCookie.from("access_token", loginResult.token())
                .httpOnly(true)
                .secure(true)
                // TODO: HTTP 배포 환경에서는 Secure 쿠키가 브라우저에 저장되지 않을 수 있어 Swagger 테스트는 Bearer 사용 권장.
                // TODO: 프론트엔드(Vercel) 쿠키 인증 적용 시 SameSite=None, Secure=true 설정 필요 (교차 도메인 대비).
                .path("/")
                .maxAge(Duration.ofSeconds(loginResult.expiresInSeconds()))
                .build();

        AdminLoginResponseDto admin = loginResult.admin();
        AdminLoginTokenResponseDto response = AdminLoginTokenResponseDto.from(admin, loginResult.token());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }
}
