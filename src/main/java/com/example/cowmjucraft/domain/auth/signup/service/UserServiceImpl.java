package com.example.cowmjucraft.domain.auth.signup.service;

import com.example.cowmjucraft.domain.account.entity.Role;
import com.example.cowmjucraft.domain.account.entity.User;
import com.example.cowmjucraft.domain.auth.signup.dto.request.SignupRequest;
import com.example.cowmjucraft.domain.auth.signup.dto.response.SignupResponse;
import com.example.cowmjucraft.domain.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SignupResponse signup(SignupRequest request) {

        // UX 차원의 사전 체크 (최종 보장은 DB unique + catch)
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicated userId");
        }

        try {
            String encoded = passwordEncoder.encode(request.getPassword());

            User user = new User(
                    request.getUserId(),
                    request.getUserName(),
                    request.getEmail(),
                    encoded,
                    Role.USER
            );

            User saved = userRepository.save(user);
            return SignupResponse.from(saved);

        } catch (DataIntegrityViolationException e) {
            // 동시성으로 중복이 들어온 경우도 여기서 잡힘 (DB unique 있을 때)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicated userId");
        }
    }
}
