package com.example.cowmjucraft.domain.auth.signin.dto;

import com.example.cowmjucraft.domain.auth.signin.domain.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String userId;
    private String email;
    private String password;
    private String userName;
    private Role role;
}
