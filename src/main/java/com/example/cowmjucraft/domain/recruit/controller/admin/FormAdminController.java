package com.example.cowmjucraft.domain.recruit.controller.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.request.*;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.*;
import com.example.cowmjucraft.domain.recruit.service.admin.FormAdminService;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import org.springframework.http.ResponseEntity;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/admin")
@RestController
public class FormAdminController implements FormAdminControllerDocs {

    private final FormAdminService formAdminService;

    @Override
    @PostMapping("/forms")
    public ResponseEntity<ApiResult<FormCreateAdminResponse>> createForm(
            @RequestBody FormCreateAdminRequest request
    ) {
        return ApiResponse.of(
                SuccessType.CREATED,
                formAdminService.createForm(request)
        );
    }

    @Override
    @DeleteMapping("/forms/{formId}")
    public ResponseEntity<ApiResult<Void>> deleteForm(@PathVariable Long formId) {
        formAdminService.deleteForm(formId);

        return ApiResponse.of(SuccessType.SUCCESS);
    }

    @Override
    @PutMapping("/forms/{formId}/open")
    public ResponseEntity<ApiResult<Void>> openForm(@PathVariable Long formId) {
        formAdminService.openForm(formId);
        return ApiResponse.of(SuccessType.SUCCESS);
    }

    @Override
    @PutMapping("/forms/{formId}/close")
    public ResponseEntity<ApiResult<Void>> closeForm(@PathVariable Long formId) {
        formAdminService.closeForm(formId);
        return ApiResponse.of(SuccessType.SUCCESS);
    }

    @Override
    @PostMapping("/forms/{formId}/questions")
    public ResponseEntity<ApiResult<AddQuestionAdminResponse>> addQuestion(
            @PathVariable Long formId,
            @RequestBody AddQuestionAdminRequest request
    ) {
        return ApiResponse.of(
                SuccessType.CREATED,
                formAdminService.addQuestion(formId, request)
        );
    }

    @Override
    @GetMapping("/forms")
    public ResponseEntity<ApiResult<List<FormListAdminResponse>>> getForms() {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                formAdminService.getForms()
        );
    }

    @Override
    @GetMapping("/forms/{formId}")
    public ResponseEntity<ApiResult<FormDetailAdminResponse>> getForm(@PathVariable Long formId) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                formAdminService.getForm(formId)
        );
    }

    @Override
    @GetMapping("/forms/{formId}/questions")
    public ResponseEntity<ApiResult<List<FormQuestionListAdminResponse>>> getFormQuestions(
            @PathVariable Long formId
    ) {
        return ApiResponse.of(
                SuccessType.SUCCESS,
                formAdminService.getFormQuestions(formId)
        );
    }

    @Override
    @DeleteMapping("/forms/{formId}/questions/{formQuestionId}")
    public ResponseEntity<ApiResult<Void>> deleteFormQuestion(
            @PathVariable Long formId,
            @PathVariable Long formQuestionId
    ) {
        formAdminService.deleteFormQuestion(formId, formQuestionId);
        return ApiResponse.of(SuccessType.SUCCESS);
    }

    @Override
    @PutMapping("/forms/{formId}/questions/{formQuestionId}")
    public ResponseEntity<ApiResult<Void>> updateFormQuestion(
            @PathVariable Long formId,
            @PathVariable Long formQuestionId,
            @RequestBody FormQuestionUpdateAdminRequest request
    ) {
        formAdminService.updateFormQuestion(formId, formQuestionId, request);
        return ApiResponse.of(SuccessType.SUCCESS);
    }

    @Override
    @PostMapping("/forms/{targetFormId}/copy")
    public ResponseEntity<ApiResult<FormCopyAdminResponse>> copyFormQuestionsOverwrite(
            @PathVariable Long targetFormId,
            @RequestBody FormCopyAdminRequest request
    ) {
        return ApiResponse.of(
                SuccessType.CREATED,
                formAdminService.copyFormQuestionsOverwrite(targetFormId, request)
        );
    }

    @Override
    @PostMapping("/forms/{formId}/notices")
    public ResponseEntity<ApiResult<AddFormNoticeAdminResponse>> addFormNotice(
            @PathVariable Long formId,
            @RequestBody FormNoticeRequest request
    ) {
        return ApiResponse.of(
                SuccessType.CREATED,
                formAdminService.addFormNotice(formId, request)
        );
    }

    @Override
    @PutMapping("/forms/{formId}/notices/{noticeId}")
    public ResponseEntity<ApiResult<Void>> updateFormNotice(
            @PathVariable Long formId,
            @PathVariable Long noticeId,
            @RequestBody FormNoticeRequest request
    ) {
        formAdminService.updateFormNotice(formId, noticeId, request);
        return ApiResponse.of(SuccessType.SUCCESS);
    }

    @Override
    @DeleteMapping("/forms/{formId}/notices/{noticeId}")
    public ResponseEntity<ApiResult<Void>> deleteFormNotice(
            @PathVariable Long formId,
            @PathVariable Long noticeId
    ) {
        formAdminService.deleteFormNotice(formId, noticeId);
        return ApiResponse.of(SuccessType.SUCCESS);
    }
}
