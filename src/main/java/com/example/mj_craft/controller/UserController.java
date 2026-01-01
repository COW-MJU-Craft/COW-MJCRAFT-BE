package com.example.mj_craft.controller;

import com.example.mj_craft.dto.UserDTO;
import com.example.mj_craft.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO signup(@RequestBody UserDTO userDTO){
        return userService.signup(userDTO);
    }
}
