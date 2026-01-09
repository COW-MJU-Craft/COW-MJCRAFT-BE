package com.example.cowmjucraft.domain.accounts.admin.account.service;

import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;
import com.example.cowmjucraft.domain.accounts.admin.repository.AdminRepository;
import com.example.cowmjucraft.domain.accounts.admin.account.dto.request.AdminAccountUpdateRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.login.dto.response.AdminLoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Transactional
@Service
public class AdminAccountService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminLoginResponseDto updateAdminAccount(AdminAccountUpdateRequestDto request) {
        Admin admin = adminRepository.findByLoginId(request.currentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.currentPassword(), admin.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (!request.currentUserId().equals(request.newUserId())
                && adminRepository.existsByLoginId(request.newUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicated userId");
        }

        if (!request.currentUserId().equals(request.newUserId())) {
            admin.updateLoginId(request.newUserId());
        }

        if (StringUtils.hasText(request.newPassword())) {
            admin.updatePassword(passwordEncoder.encode(request.newPassword()));
        }

        return AdminLoginResponseDto.from(admin);
    }
}
