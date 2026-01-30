package com.example.cowmjucraft.domain.introduce.controller.admin;

import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceDetailUpsertRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceMainUpsertRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroducePresignPutRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroducePresignPutResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceDetailResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceMainResponseDto;
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

    @GetMapping("/main")
    @Override
    public ApiResult<AdminIntroduceMainResponseDto> getMain() {
        return ApiResult.success(SuccessType.SUCCESS, introduceService.adminGetMain());
    }

    @PutMapping("/main")
    @Override
    public ApiResult<AdminIntroduceMainResponseDto> upsertMain(
            @Valid @RequestBody AdminIntroduceMainUpsertRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, introduceService.adminUpsertMain(request));
    }

    @GetMapping
    @Override
    public ApiResult<AdminIntroduceDetailResponseDto> getDetail() {
        return ApiResult.success(SuccessType.SUCCESS, introduceService.adminGetDetail());
    }

    @PutMapping
    @Override
    public ApiResult<AdminIntroduceDetailResponseDto> upsertDetail(
            @Valid @RequestBody AdminIntroduceDetailUpsertRequestDto request
    ) {
        return ApiResult.success(SuccessType.SUCCESS, introduceService.adminUpsertDetail(request));
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
