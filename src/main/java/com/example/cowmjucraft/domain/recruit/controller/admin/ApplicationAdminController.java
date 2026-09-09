package com.example.cowmjucraft.domain.recruit.controller.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.request.ApplicationResultUpdateAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.ApplicationDetailAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.ApplicationListAdminResponse;
import com.example.cowmjucraft.domain.recruit.service.admin.ApplicationAdminService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import java.util.List;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/admin")
@RestController
public class ApplicationAdminController implements ApplicationAdminControllerDocs {

    private final ApplicationAdminService applicationAdminService;

    @Override
    @GetMapping("/forms/{formId}/applications")
    public ResponseEntity<ApiResult<List<ApplicationListAdminResponse>>> getApplicationsByFormId(@PathVariable Long formId) {
        return ApiResponse.of(SuccessType.SUCCESS, applicationAdminService.getApplicationsByFormId(formId));
    }

    @Override
    @GetMapping("/forms/{formId}/applications/{applicationId}")
    public ResponseEntity<ApiResult<ApplicationDetailAdminResponse>> getApplicationByFormId(
            @PathVariable Long formId,
            @PathVariable Long applicationId
    ) {
        return ApiResponse.of(SuccessType.SUCCESS, applicationAdminService.getApplication(formId, applicationId));
    }

    @Override
    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<ApiResult<Void>> deleteApplication(@PathVariable Long applicationId) {
        applicationAdminService.deleteApplication(applicationId);
        return ApiResponse.of(SuccessType.SUCCESS);
    }

    @Override
    @PutMapping("/applications/{applicationId}/result")
    public ResponseEntity<ApiResult<Void>> updateResult(
            @PathVariable Long applicationId,
            @RequestBody ApplicationResultUpdateAdminRequest request
    ) {
        applicationAdminService.updateResult(applicationId, request);
        return ApiResponse.of(SuccessType.SUCCESS);
    }
}
