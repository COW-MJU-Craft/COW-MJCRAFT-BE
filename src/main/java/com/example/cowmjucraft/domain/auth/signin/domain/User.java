package com.example.cowmjucraft.domain.auth.signin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String userId;
   // studentId가 될 수도 있지만, 아직 확정이 아니라서 userId로 했습니다.

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private Role role;

    public User(String userId, String userName, String email, String password, Role role){
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
