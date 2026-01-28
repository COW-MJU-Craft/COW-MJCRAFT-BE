package com.example.cowmjucraft.domain.sns.controller.admin;

import com.example.cowmjucraft.domain.sns.dto.request.SnsAdminRequestDto;
import com.example.cowmjucraft.domain.sns.entity.SnsType;
import com.example.cowmjucraft.domain.sns.service.SnsService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/sns")
public class SnsAdminController implements SnsAdminControllerDocs {

    private final SnsService snsService;

    @PutMapping("/kakao")
    @Override
    public ApiResult<?> upsertKakao(@Valid @RequestBody SnsAdminRequestDto request) {
        snsService.upsert(SnsType.KAKAO, request);
        return ApiResult.success(SuccessType.SUCCESS);
    }

    @PutMapping("/instagram")
    @Override
    public ApiResult<?> upsertInstagram(@Valid @RequestBody SnsAdminRequestDto request) {
        snsService.upsert(SnsType.INSTAGRAM, request);
        return ApiResult.success(SuccessType.SUCCESS);
    }
}
