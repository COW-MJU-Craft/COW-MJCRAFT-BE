package com.example.cowmjucraft.domain.introduce.controller.client;

import com.example.cowmjucraft.domain.introduce.dto.response.IntroduceDetailResponse;
import com.example.cowmjucraft.domain.introduce.dto.response.IntroduceMainSummaryResponse;
import com.example.cowmjucraft.domain.introduce.service.IntroduceService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/introduce")
public class IntroduceController implements IntroduceControllerDocs {

    private final IntroduceService introduceService;

    @GetMapping("/main")
    @Override
    public ApiResult<IntroduceMainSummaryResponse> getMainSummary() {
        return ApiResult.success(SuccessType.SUCCESS, introduceService.getMainSummary());
    }

    @GetMapping
    @Override
    public ApiResult<IntroduceDetailResponse> getDetail() {
        return ApiResult.success(SuccessType.SUCCESS, introduceService.getDetail());
    }
}
