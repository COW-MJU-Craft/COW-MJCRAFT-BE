package com.example.cowmjucraft.domain.sns.controller.client;

import com.example.cowmjucraft.domain.sns.dto.response.SnsResponseDto;
import com.example.cowmjucraft.domain.sns.entity.SnsType;
import com.example.cowmjucraft.domain.sns.service.SnsService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResult<SnsResponseDto>> getKakaoLink() {
        return ApiResponse.of(SuccessType.SUCCESS, snsService.getLink(SnsType.KAKAO));
    }

    @GetMapping("/instagram")
    @Override
    public ResponseEntity<ApiResult<SnsResponseDto>> getInstagramLink() {
        return ApiResponse.of(SuccessType.SUCCESS, snsService.getLink(SnsType.INSTAGRAM));
    }
}
