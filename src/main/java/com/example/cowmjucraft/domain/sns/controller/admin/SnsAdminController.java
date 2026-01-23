package com.example.cowmjucraft.domain.sns.controller.admin;

import com.example.cowmjucraft.domain.sns.dto.request.SnsAdminRequestDto;
import com.example.cowmjucraft.domain.sns.service.SnsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class SnsAdminController implements SnsAdminControllerDocs {

    private final SnsService snsService;

    @PutMapping("/introduce/sns")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void replaceSnsLinks(@Valid @RequestBody List<SnsAdminRequestDto> requests) {
        snsService.replaceAll(requests);
    }
}