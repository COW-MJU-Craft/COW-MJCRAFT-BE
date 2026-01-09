package com.example.cowmjucraft.domain.accounts.user.signup.service;

import com.example.cowmjucraft.domain.accounts.user.entity.Member;
import com.example.cowmjucraft.domain.accounts.user.signup.dto.request.UserSignupRequestDto;
import com.example.cowmjucraft.domain.accounts.user.signup.dto.response.UserSignupResponseDto;
import com.example.cowmjucraft.domain.accounts.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserSignupResponseDto signup(UserSignupRequestDto request) {

        // UX 차원의 사전 체크 (최종 보장은 DB unique + catch)
        if (memberRepository.existsByUserId(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicated userId");
        }

        try {
            String encoded = passwordEncoder.encode(request.getPassword());

            Member member = new Member(
                    request.getUserId(),
                    request.getUserName(),
                    request.getEmail(),
                    encoded
            );

            Member saved = memberRepository.save(member);
            return UserSignupResponseDto.from(saved);

        } catch (DataIntegrityViolationException e) {
            // 동시성으로 중복이 들어온 경우도 여기서 잡힘 (DB unique 있을 때)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicated userId");
        }
    }
}
