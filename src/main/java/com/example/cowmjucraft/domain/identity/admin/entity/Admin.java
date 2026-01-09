package com.example.cowmjucraft.domain.identity.admin.entity;

import com.example.cowmjucraft.domain.identity.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_ADMIN;

    public Admin(String loginId, String password, String email) {
        this.loginId = loginId;
        this.password = password;
        this.email = email;
    }

    public void updateLoginId(String loginId) {
        this.loginId = loginId;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateEmail(String email) {
        this.email = email;
    }
}
