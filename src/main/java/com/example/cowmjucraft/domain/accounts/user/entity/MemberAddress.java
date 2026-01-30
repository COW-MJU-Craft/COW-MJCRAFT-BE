package com.example.cowmjucraft.domain.accounts.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "member_addresses")
public class MemberAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String postCode;

    @Column(nullable = false)
    private String address;

    public MemberAddress(
            Member member,
            String recipientName,
            String phoneNumber,
            String postCode,
            String address
    ) {
        this.member = member;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.postCode = postCode;
        this.address = address;
    }

    public void update(
            String recipientName,
            String phoneNumber,
            String postCode,
            String address
    ) {
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.postCode = postCode;
        this.address = address;
    }
}
