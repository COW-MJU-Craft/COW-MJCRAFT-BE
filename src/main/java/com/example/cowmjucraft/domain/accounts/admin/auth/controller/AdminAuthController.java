package com.example.cowmjucraft.domain.accounts.admin.auth.controller;

import com.example.cowmjucraft.domain.accounts.admin.auth.dto.request.AdminLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.auth.dto.response.AdminLoginTokenResponseDto;
import com.example.cowmjucraft.domain.accounts.admin.auth.service.AdminAuthService;
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
public class AdminAuthController implements AdminAuthControllerDocs {

    private final AdminAuthService adminAuthService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<AdminLoginTokenResponseDto> login(@RequestBody AdminLoginRequestDto request) {
        AdminAuthService.LoginResult loginResult = adminAuthService.login(request);
        AdminLoginTokenResponseDto response = AdminLoginTokenResponseDto.from(loginResult.admin(), loginResult.token());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        adminAuthService.logout();
        return ResponseEntity.noContent().build();
    }
}
