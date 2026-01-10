package com.example.cowmjucraft.domain.introduce.intro.controller.client;

import com.example.cowmjucraft.domain.introduce.intro.dto.response.IntroduceResponseDto;
import com.example.cowmjucraft.domain.introduce.intro.service.IntroduceContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class IntroduceController implements IntroduceControllerDocs {

    private final IntroduceContentService introduceContentService;

    @GetMapping("/introduce/information")
    @Override
    public IntroduceResponseDto getIntroduceContent() {
        return introduceContentService.getIntroduceContent();
    }
}
