package com.example.cowmjucraft.domain.recruit.controller.client;

import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationCreateRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationReadRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationUpdateRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.request.ResultReadRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationCreateResponse;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationReadResponse;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationUpdateResponse;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ResultReadResponse;
import com.example.cowmjucraft.domain.recruit.service.client.ApplicationService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
public class ApplicationController implements ApplicationControllerDocs {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    @PostMapping("/application")
    public ApiResult<ApplicationCreateResponse> createApplication(
            @RequestBody ApplicationCreateRequest request
    ) {
        return ApiResult.success(
                SuccessType.CREATED,
                applicationService.create(request)
        );
    }

    @Override
    @PostMapping("/application/read")
    public ApiResult<ApplicationReadResponse> readApplication(
            @RequestBody ApplicationReadRequest request
    ) {
        return ApiResult.success(
                SuccessType.SUCCESS,
                applicationService.read(request)
        );
    }

    @Override
    @PutMapping("/application")
    public ApiResult<ApplicationUpdateResponse> updateApplication(
            @RequestBody ApplicationUpdateRequest request
    ) {
        return ApiResult.success(
                SuccessType.SUCCESS,
                applicationService.update(request)
        );
    }

    @Override
    @PostMapping("/result")
    public ApiResult<ResultReadResponse> readResult(
            @RequestBody ResultReadRequest request
    ) {
        return ApiResult.success(
                SuccessType.SUCCESS,
                applicationService.readResult(request)
        );
    }
}
