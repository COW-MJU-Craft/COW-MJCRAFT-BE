package com.example.cowmjucraft.domain.order.controller.admin;

import com.example.cowmjucraft.domain.order.dto.request.AdminOrderTrackingInformationUpdateRequestDto;
import com.example.cowmjucraft.domain.order.dto.response.AdminOrderTrackingInformationResponseDto;
import com.example.cowmjucraft.domain.order.service.AdminOrderTrackingInformationService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/projects/{projectId}/orders")
public class AdminProjectOrderTrackingController implements AdminProjectOrderTrackingControllerDocs {

    private final AdminOrderTrackingInformationService adminOrderTrackingInformationService;

    @PutMapping("/{orderId}/tracking-information")
    @Override
    public ResponseEntity<ApiResult<AdminOrderTrackingInformationResponseDto>> updateTrackingInformation(
            @PathVariable Long projectId,
            @PathVariable Long orderId,
            @Valid @RequestBody AdminOrderTrackingInformationUpdateRequestDto request
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                adminOrderTrackingInformationService.updateTrackingInformation(
                        projectId,
                        orderId,
                        request.trackingInformation()
                )
        );
    }
}
