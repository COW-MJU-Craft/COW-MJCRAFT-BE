package com.example.cowmjucraft.domain.auth.signin.service;

import com.example.cowmjucraft.domain.auth.signin.dto.UserDTO;

public interface UserService {
    UserDTO signup(UserDTO userDTO);
}
