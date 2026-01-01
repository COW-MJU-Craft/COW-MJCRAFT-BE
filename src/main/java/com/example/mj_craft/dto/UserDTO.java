package com.example.mj_craft.dto;

import com.example.mj_craft.domain.Role;
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
