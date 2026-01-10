package com.example.cowmjucraft.domain.introduce.intro.controller.admin;

import com.example.cowmjucraft.domain.introduce.intro.dto.request.IntroduceAdminRequestDto;
import com.example.cowmjucraft.domain.introduce.intro.service.IntroduceContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class IntroduceAdminController implements IntroduceAdminControllerDocs {

    private final IntroduceContentService introduceContentService;

    @PutMapping("/introduce/information")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void replaceIntroduceContent(@Valid @RequestBody IntroduceAdminRequestDto request) {
        introduceContentService.replaceIntroduceContent(request);
    }
}
