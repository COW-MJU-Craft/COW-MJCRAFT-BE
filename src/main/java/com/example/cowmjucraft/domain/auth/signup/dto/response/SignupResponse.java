package com.example.cowmjucraft.domain.auth.signup.dto.response;

import com.example.cowmjucraft.domain.account.entity.Role;
import com.example.cowmjucraft.domain.account.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {
    private final Long id;
    private final String userId;
    private final String email;
    private final String userName;
    private final Role role;

    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getUserId(),
                user.getEmail(),
                user.getUserName(),
                user.getRole()
        );
    }
}
