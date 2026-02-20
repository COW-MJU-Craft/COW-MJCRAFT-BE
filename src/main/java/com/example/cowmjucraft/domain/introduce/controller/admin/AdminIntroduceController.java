package com.example.cowmjucraft.domain.introduce.controller.admin;

import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceDetailUpsertRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceMainUpsertRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroducePresignPutRequestDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroducePresignPutResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceDetailResponseDto;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceMainResponseDto;
import com.example.cowmjucraft.domain.introduce.service.IntroduceService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResult<AdminIntroduceMainResponseDto>> getMain() {
        return ApiResponse.of(SuccessType.SUCCESS, introduceService.adminGetMain());
    }

    @PutMapping("/main")
    @Override
    public ResponseEntity<ApiResult<AdminIntroduceMainResponseDto>> upsertMain(
            @Valid @RequestBody AdminIntroduceMainUpsertRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, introduceService.adminUpsertMain(request));
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResult<AdminIntroduceDetailResponseDto>> getDetail() {
        return ApiResponse.of(SuccessType.SUCCESS, introduceService.adminGetDetail());
    }

    @PutMapping
    @Override
    public ResponseEntity<ApiResult<AdminIntroduceDetailResponseDto>> upsertDetail(
            @Valid @RequestBody AdminIntroduceDetailUpsertRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, introduceService.adminUpsertDetail(request));
    }

    @PostMapping("/presign-put/hero-logos")
    @Override
    public ResponseEntity<ApiResult<AdminIntroducePresignPutResponseDto>> presignHeroLogo(
            @Valid @RequestBody AdminIntroducePresignPutRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, introduceService.createHeroLogoPresignPut(request));
    }

    @PostMapping("/presign-put/sections")
    @Override
    public ResponseEntity<ApiResult<AdminIntroducePresignPutResponseDto>> presignSection(
            @Valid @RequestBody AdminIntroducePresignPutRequestDto request
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, introduceService.createSectionPresignPut(request));
    }
}
