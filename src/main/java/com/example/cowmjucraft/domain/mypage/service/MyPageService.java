package com.example.cowmjucraft.domain.mypage.service;

import com.example.cowmjucraft.domain.accounts.user.entity.Member;
import com.example.cowmjucraft.domain.accounts.user.entity.MemberAddress;
import com.example.cowmjucraft.domain.accounts.user.entity.SocialProvider;
import com.example.cowmjucraft.domain.accounts.user.repository.MemberRepository;
import com.example.cowmjucraft.domain.mypage.dto.request.MyPageAddressRequestDto;
import com.example.cowmjucraft.domain.mypage.dto.response.MyPageAddressResponseDto;
import com.example.cowmjucraft.domain.mypage.dto.response.MyPageResponseDto;
import com.example.cowmjucraft.domain.mypage.exception.MyPageErrorType;
import com.example.cowmjucraft.domain.mypage.exception.MyPageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyPageService {

    private final MemberRepository memberRepository;

    public MyPageService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public MyPageResponseDto getMyPage(Long memberId) {
        Member member = getMember(memberId);
        return toMyPageResponse(member);
    }

    @Transactional
    public MyPageAddressResponseDto createAddress(Long memberId, MyPageAddressRequestDto request) {
        Member member = getMember(memberId);
        if (member.getAddress() != null) {
            throw new MyPageException(MyPageErrorType.ADDRESS_ALREADY_EXISTS);
        }
        member.upsertAddress(
                request.recipientName(),
                request.phoneNumber(),
                request.postCode(),
                request.address()
        );
        return toAddressResponse(member.getAddress());
    }

    @Transactional
    public MyPageAddressResponseDto updateAddress(Long memberId, MyPageAddressRequestDto request) {
        Member member = getMember(memberId);
        if (member.getAddress() == null) {
            throw new MyPageException(MyPageErrorType.ADDRESS_NOT_FOUND);
        }
        member.upsertAddress(
                request.recipientName(),
                request.phoneNumber(),
                request.postCode(),
                request.address()
        );
        return toAddressResponse(member.getAddress());
    }

    @Transactional
    public void deleteAddress(Long memberId) {
        Member member = getMember(memberId);
        if (member.getAddress() == null) {
            throw new MyPageException(MyPageErrorType.ADDRESS_NOT_FOUND);
        }
        member.clearAddress();
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MyPageException(MyPageErrorType.MEMBER_NOT_FOUND));
    }

    private MyPageResponseDto toMyPageResponse(Member member) {
        SocialProvider provider = member.getProvider();
        return new MyPageResponseDto(
                member.getId(),
                member.getUserName(),
                member.getEmail(),
                provider,
                toAddressResponse(member.getAddress())
        );
    }

    private MyPageAddressResponseDto toAddressResponse(MemberAddress address) {
        if (address == null) {
            return null;
        }
        return new MyPageAddressResponseDto(
                address.getRecipientName(),
                address.getPhoneNumber(),
                address.getPostCode(),
                address.getAddress()
        );
    }
}
