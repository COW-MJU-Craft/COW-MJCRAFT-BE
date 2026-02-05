package com.example.cowmjucraft.domain.recruit.controller.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.request.ApplicationResultUpdateAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.ApplicationDetailAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.ApplicationListAdminResponse;
import com.example.cowmjucraft.domain.recruit.service.admin.ApplicationAdminService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/admin")
@RestController
public class ApplicationAdminController implements ApplicationAdminControllerDocs {

    private final ApplicationAdminService applicationAdminService;

    @Override
    @GetMapping("/forms/{formId}/applications")
    public ApiResult<List<ApplicationListAdminResponse>> getApplicationsByFormId(@PathVariable Long formId) {
        return ApiResult.success(SuccessType.SUCCESS, applicationAdminService.getApplicationsByFormId(formId));
    }

    @Override
    @GetMapping("/forms/{formId}/applications/{applicationId}")
    public ApiResult<ApplicationDetailAdminResponse> getApplicationByFormId(
            @PathVariable Long formId,
            @PathVariable Long applicationId
    ) {
        return ApiResult.success(SuccessType.SUCCESS, applicationAdminService.getApplication(formId, applicationId));
    }

    @Override
    @DeleteMapping("/applications/{applicationId}")
    public ApiResult<?> deleteApplication(@PathVariable Long applicationId) {
        applicationAdminService.deleteApplication(applicationId);
        return ApiResult.success(SuccessType.SUCCESS);
    }

    @Override
    @PutMapping("/applications/{applicationId}/result")
    public ApiResult<?> updateResult(
            @PathVariable Long applicationId,
            @RequestBody ApplicationResultUpdateAdminRequest request
    ) {
        applicationAdminService.updateResult(applicationId, request);
        return ApiResult.success(SuccessType.SUCCESS);
    }
}
