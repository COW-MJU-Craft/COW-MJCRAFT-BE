package com.example.cowmjucraft.domain.accounts.user.entity;

import com.example.cowmjucraft.domain.accounts.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "provider_id"})
        }
)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private SocialProvider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private MemberAddress address;

    public Member(String userName, String email) {
        this.userName = userName;
        this.email = email;

        this.userId = UUID.randomUUID().toString();
    }

    public void updateUserName(String userName) {
        this.userName = userName;
    }

    public void updateSocial(SocialProvider provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }

    public void ensureUserId() {
        if (this.userId == null || this.userId.isBlank()) {
            this.userId = UUID.randomUUID().toString();
        }
    }

    public void upsertAddress(
            String recipientName,
            String phoneNumber,
            String postCode,
            String address
    ) {
        if (this.address == null) {
            this.address = new MemberAddress(this, recipientName, phoneNumber, postCode, address);
            return;
        }
        this.address.update(recipientName, phoneNumber, postCode, address);
    }

    public void clearAddress() {
        this.address = null;
    }
}
