package com.example.cowmjucraft.domain.accounts.admin.login.controller;

import com.example.cowmjucraft.domain.accounts.admin.login.dto.request.AdminLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.login.dto.response.AdminLoginResponseDto;
import com.example.cowmjucraft.domain.accounts.admin.login.service.AdminLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminLoginController implements AdminLoginControllerDocs {

    private final AdminLoginService adminLoginService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponseDto> login(@RequestBody AdminLoginRequestDto request) {
        // TODO: 관리자 로그인 엔드포인트에 rate limit 적용 필요 (무차별 대입 방지 목적).
        AdminLoginService.LoginResult loginResult = adminLoginService.login(request);
        AdminLoginResponseDto response = AdminLoginResponseDto.from(loginResult.admin(), loginResult.token());
        return ResponseEntity.ok(response);
    }
}
