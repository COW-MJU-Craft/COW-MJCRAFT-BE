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

        introduceContentRepository.deleteAllInBatch();

        IntroduceContent content = new IntroduceContent(
                safeRequest.title(),
                safeRequest.content()
        );

        introduceContentRepository.save(content);
    }
}
