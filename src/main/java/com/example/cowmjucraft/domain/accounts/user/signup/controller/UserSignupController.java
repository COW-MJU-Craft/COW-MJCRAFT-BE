package com.example.cowmjucraft.domain.accounts.user.signup.controller;

import com.example.cowmjucraft.domain.accounts.user.signup.dto.request.UserSignupRequestDto;
import com.example.cowmjucraft.domain.accounts.user.signup.dto.response.UserSignupResponseDto;
import com.example.cowmjucraft.domain.accounts.user.signup.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserSignupController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSignupResponseDto signup(@RequestBody UserSignupRequestDto request){
        return userService.signup(request);
    }
}
