package com.example.cowmjucraft.domain.payout.controller.client;

import com.example.cowmjucraft.domain.payout.dto.response.PayoutDetailResponse;
import com.example.cowmjucraft.domain.payout.service.client.PayoutService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projects")
public class ProjectPayoutController implements ProjectPayoutControllerDocs{

    private final PayoutService payoutService;

    @Override
    @GetMapping("/{projectId}/payout")
    public ResponseEntity<ApiResult<PayoutDetailResponse>> getPayoutDetailByProjectId(@PathVariable Long projectId) {
        return ApiResponse.of(SuccessType.SUCCESS, payoutService.getPayoutDetailByProjectId(projectId));
    }
}
