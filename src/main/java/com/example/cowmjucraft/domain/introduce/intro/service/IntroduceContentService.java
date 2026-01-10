package com.example.cowmjucraft.domain.introduce.intro.service;

import com.example.cowmjucraft.domain.introduce.intro.dto.request.IntroduceAdminRequestDto;
import com.example.cowmjucraft.domain.introduce.intro.dto.response.IntroduceResponseDto;
import com.example.cowmjucraft.domain.introduce.intro.entity.IntroduceContent;
import com.example.cowmjucraft.domain.introduce.intro.repository.IntroduceContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class IntroduceContentService {

    private final IntroduceContentRepository introduceContentRepository;

    @Transactional(readOnly = true)
    public IntroduceResponseDto getIntroduceContent() {
        return introduceContentRepository.findTopByOrderByIdAsc()
                .map(IntroduceResponseDto::from)
                .orElse(null);
    }

    @Transactional
    public void replaceIntroduceContent(IntroduceAdminRequestDto request) {
        IntroduceAdminRequestDto safeRequest =
                Objects.requireNonNull(request, "request must not be null");

        // TODO: replace 방식 대신 단일 row 업데이트로 개선해 이력/잠금 이슈를 줄일 수 있음.
        introduceContentRepository.deleteAllInBatch();

        // TODO: mediaId 존재/ACTIVE/usageType 일치 검증을 도입해 잘못된 참조를 사전에 차단하는 방식 검토 필요.
        // TODO: PENDING/DELETED 상태의 Media 정리를 위한 배치/비동기 정책을 Media 도메인에 추가 검토 필요.
        IntroduceContent content = new IntroduceContent(
                safeRequest.title(),
                safeRequest.content(),
                safeRequest.logoMediaId(),
                safeRequest.bannerMediaId()
        );

        introduceContentRepository.save(content);
    }
}
