package com.example.cowmjucraft.domain.sns.controller.client;

import com.example.cowmjucraft.domain.sns.dto.response.SnsResponseDto;
import com.example.cowmjucraft.domain.sns.entity.SnsType;
import com.example.cowmjucraft.domain.sns.service.SnsService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sns")
public class SnsController implements SnsControllerDocs {

    private final SnsService snsService;

    @GetMapping("/kakao")
    @Override
    public ApiResult<SnsResponseDto> getKakaoLink() {
        return ApiResult.success(SuccessType.SUCCESS, snsService.getLink(SnsType.KAKAO));
    }

    @GetMapping("/instagram")
    @Override
    public ApiResult<SnsResponseDto> getInstagramLink() {
        return ApiResult.success(SuccessType.SUCCESS, snsService.getLink(SnsType.INSTAGRAM));
    }
}
