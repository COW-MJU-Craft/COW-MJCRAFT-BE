package com.example.cowmjucraft.domain.introduce.controller.admin;

import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceUpsertRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroducePresignPutRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroducePresignPutResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceResponseDto;
import com.example.cowmjucraft.domain.introduce.service.IntroduceService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/introduce")
public class AdminIntroduceController implements AdminIntroduceControllerDocs {

    private final IntroduceService introduceService;

    @GetMapping
    @Override
    public ApiResult<AdminIntroduceResponseDto> getIntroduce() {
        return ApiResult.success(SuccessType.SUCCESS, introduceService.adminGet());
    }

    @PutMapping
    @Override
    public ApiResult<AdminIntroduceResponseDto> upsertIntroduce(
            @Valid @RequestBody AdminIntroduceUpsertRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, introduceService.adminUpsert(request));
    }

    @PostMapping("/presign-put/hero-logos")
    @Override
    public ApiResult<AdminIntroducePresignPutResponseDto> presignHeroLogo(
            @Valid @RequestBody AdminIntroducePresignPutRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, introduceService.createHeroLogoPresignPut(request));
    }

    @PostMapping("/presign-put/sections")
    @Override
    public ApiResult<AdminIntroducePresignPutResponseDto> presignSection(
            @Valid @RequestBody AdminIntroducePresignPutRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, introduceService.createSectionPresignPut(request));
    }
}
