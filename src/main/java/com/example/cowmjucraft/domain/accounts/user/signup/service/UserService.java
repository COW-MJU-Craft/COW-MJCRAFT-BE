package com.example.cowmjucraft.domain.accounts.user.signup.service;

import com.example.cowmjucraft.domain.accounts.user.signup.dto.response.UserSignupResponseDto;
import com.example.cowmjucraft.domain.accounts.user.signup.dto.request.UserSignupRequestDto;

public interface UserService {
    UserSignupResponseDto signup(UserSignupRequestDto request);
}
