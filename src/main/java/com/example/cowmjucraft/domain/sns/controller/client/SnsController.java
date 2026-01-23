package com.example.cowmjucraft.domain.sns.controller.client;

import com.example.cowmjucraft.domain.sns.dto.response.SnsResponseDto;
import com.example.cowmjucraft.domain.sns.service.SnsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class SnsController implements SnsControllerDocs {

    private final SnsService snsService;

    @GetMapping("/introduce/sns")
    @Override
    public List<SnsResponseDto> getSnsLinks() {
        return snsService.getActiveSnsLinks();
    }
}