package com.example.cowmjucraft.domain.accounts.admin.login.service;

import com.example.cowmjucraft.domain.accounts.admin.login.dto.request.AdminLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.login.dto.response.AdminLoginResponseDto;
import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;
import com.example.cowmjucraft.domain.accounts.admin.repository.AdminRepository;
import com.example.cowmjucraft.global.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AdminLoginService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResult login(AdminLoginRequestDto request) {
        Admin admin = adminRepository.findByLoginId(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtTokenProvider.generateAdminToken(admin.getLoginId());
        AdminLoginResponseDto response = AdminLoginResponseDto.from(admin);

        return new LoginResult(response, token, jwtTokenProvider.getExpirationSeconds());
    }

    public record LoginResult(
            AdminLoginResponseDto admin,
            String token,
            long expiresInSeconds
    ) {
    }
}
