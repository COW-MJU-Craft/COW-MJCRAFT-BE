package com.example.cowmjucraft.domain.accounts.user.signup.dto.response;

import com.example.cowmjucraft.domain.accounts.user.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSignupResponseDto {
    private final Long id;
    private final String userId;
    private final String email;
    private final String userName;

    public static UserSignupResponseDto from(Member member) {
        return new UserSignupResponseDto(
                member.getId(),
                member.getUserId(),
                member.getEmail(),
                member.getUserName()
        );
    }
}