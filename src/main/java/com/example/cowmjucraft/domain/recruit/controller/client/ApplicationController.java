package com.example.cowmjucraft.domain.recruit.controller.client;

import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationCreateRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationReadRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationUpdateRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.request.ResultReadRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationCreateResponse;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationFormInfoResponse;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationReadResponse;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationUpdateResponse;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ResultReadResponse;
import com.example.cowmjucraft.domain.recruit.service.client.ApplicationService;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class ApplicationController implements ApplicationControllerDocs {

    private final ApplicationService applicationService;

    @Override
    @PostMapping("/application")
    public ResponseEntity<ApiResult<ApplicationCreateResponse>> createApplication(
            @Valid @RequestBody ApplicationCreateRequest request
    ) {
        return ApiResponse.of(
                SuccessType.CREATED,
                applicationService.create(request)
        );
    }

    @Override
    @PostMapping("/application/read")
    public ResponseEntity<ApiResult<ApplicationReadResponse>> readApplication(
            @RequestBody ApplicationReadRequest request
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                applicationService.read(request)
        );
    }

    @Override
    @PutMapping("/application")
    public ResponseEntity<ApiResult<ApplicationUpdateResponse>> updateApplication(
            @Valid @RequestBody ApplicationUpdateRequest request
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                applicationService.update(request)
        );
    }

    @Override
    @PostMapping("/result")
    public ResponseEntity<ApiResult<ResultReadResponse>> readResult(
            @RequestBody ResultReadRequest request
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                applicationService.readResult(request)
        );
    }

    @Override
    @PostMapping("/forms/files")
    public ResponseEntity<ApiResult<S3PresignFacade.PresignPutBatchResult>> presignFile(
            @RequestBody List<S3PresignFacade.PresignPutFile> request
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                applicationService.createAnswerFilePresignPut(request)
        );
    }

    @Override
    @GetMapping("/application/form")
    public ResponseEntity<ApiResult<ApplicationFormInfoResponse>> getOpenForm() {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                applicationService.getOpenFormInfo()
        );
    }
}
