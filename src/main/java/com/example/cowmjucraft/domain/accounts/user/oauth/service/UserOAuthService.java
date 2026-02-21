package com.example.cowmjucraft.domain.accounts.user.oauth.service;

import com.example.cowmjucraft.domain.accounts.user.entity.Member;
import com.example.cowmjucraft.domain.accounts.user.entity.SocialProvider;
import com.example.cowmjucraft.domain.accounts.user.oauth.client.KakaoOAuthClient;
import com.example.cowmjucraft.domain.accounts.user.oauth.client.NaverOAuthClient;
import com.example.cowmjucraft.domain.accounts.user.oauth.dto.request.KakaoLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.dto.request.NaverLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.user.oauth.dto.response.UserSocialLoginResponseDto;
import com.example.cowmjucraft.domain.accounts.user.repository.MemberRepository;
import com.example.cowmjucraft.global.config.jwt.JwtTokenProvider;
import com.example.cowmjucraft.domain.accounts.exception.AccountErrorType;
import com.example.cowmjucraft.domain.accounts.exception.AccountException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@RequiredArgsConstructor
@Transactional
@Service
public class UserOAuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final NaverOAuthClient naverOAuthClient;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public UserSocialLoginResponseDto loginWithNaver(NaverLoginRequestDto request) {
        NaverOAuthClient.NaverUserInfo userInfo = naverOAuthClient.fetchUserInfo(request.code(), request.state());
        return issueToken(SocialProvider.NAVER, userInfo.providerId(), userInfo.email(), userInfo.nickname());
    }

    public UserSocialLoginResponseDto loginWithKakao(KakaoLoginRequestDto request) {
        KakaoOAuthClient.KakaoUserInfo userInfo = kakaoOAuthClient.fetchUserInfo(request.code());
        return issueToken(SocialProvider.KAKAO, userInfo.providerId(), userInfo.email(), userInfo.nickname());
    }

    private UserSocialLoginResponseDto issueToken(
            SocialProvider provider,
            String providerId,
            String email,
            String nickname
    ) {
        if (providerId == null || providerId.isBlank()) {
            throw new AccountException(AccountErrorType.INVALID_SOCIAL_USER_ID);
        }

        Member member = memberRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> createOrLinkMember(provider, providerId, email, nickname));

        String token = jwtTokenProvider.generateMemberToken(member.getId());
        return new UserSocialLoginResponseDto(member.getId(), member.getUserName(), member.getEmail(), token);
    }

    private Member createOrLinkMember(
            SocialProvider provider,
            String providerId,
            String email,
            String nickname
    ) {
        if (email != null && !email.isBlank()) {
            Member byEmail = memberRepository.findByEmail(email).orElse(null);
            if (byEmail != null) {
                byEmail.updateSocial(provider, providerId);

                byEmail.ensureUserId();

                if (nickname != null && !nickname.isBlank()) {
                    byEmail.updateUserName(nickname);
                }
                return byEmail;
            }
        }

        String safeEmail = (email != null && !email.isBlank())
                ? email
                : provider.name().toLowerCase(Locale.ROOT) + "_" + providerId + "@social.local";
        String userName = (nickname != null && !nickname.isBlank())
                ? nickname
                : provider.name().toLowerCase(Locale.ROOT) + "_" + providerId;

        Member member = new Member(userName, safeEmail);
        member.updateSocial(provider, providerId);
        return memberRepository.save(member);
    }
}
