package com.example.cowmjucraft.domain.introduce.controller.admin;

import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceUpsertRequest;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceResponse;
import com.example.cowmjucraft.domain.introduce.service.IntroduceService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ApiResult<AdminIntroduceResponse> getIntroduce() {
        // TODO: apply admin authorization policy
        return ApiResult.success(SuccessType.SUCCESS, introduceService.adminGet());
    }

    @PutMapping
    @Override
    public ApiResult<AdminIntroduceResponse> upsertIntroduce(
            @Valid @RequestBody AdminIntroduceUpsertRequest request
    ) {
        // TODO: apply admin authorization policy
        return ApiResult.success(SuccessType.SUCCESS, introduceService.adminUpsert(request));
    }
}
