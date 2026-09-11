package com.example.cowmjucraft.domain.recruit.controller.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.request.AddQuestionAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.request.FormCopyAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.request.FormCreateAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.request.FormNoticeRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.request.FormQuestionUpdateAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.AddFormNoticeAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.AddQuestionAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.FormCopyAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.FormCreateAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.FormDetailAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.FormListAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.FormQuestionListAdminResponse;
import com.example.cowmjucraft.domain.recruit.service.admin.FormAdminService;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/admin")
@RestController
public class FormAdminController implements FormAdminControllerDocs {

    private final FormAdminService formAdminService;

    @Valid
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
