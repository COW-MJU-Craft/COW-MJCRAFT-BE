package com.example.cowmjucraft.domain.auth.login.service;

import com.example.cowmjucraft.domain.account.entity.Role;
import com.example.cowmjucraft.domain.account.entity.User;
import com.example.cowmjucraft.domain.account.repository.UserRepository;
import com.example.cowmjucraft.domain.auth.login.dto.request.AdminLoginRequestDto;
import com.example.cowmjucraft.domain.auth.login.dto.response.AdminLoginResponseDto;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResult login(AdminLoginRequestDto request) {
        User user = userRepository.findByUserId(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getRole());
        AdminLoginResponseDto response = AdminLoginResponseDto.from(user);

        return new LoginResult(response, token, jwtTokenProvider.getExpirationSeconds());
    }

    public record LoginResult(
            AdminLoginResponseDto admin,
            String token,
            long expiresInSeconds
    ) {
    }
}
