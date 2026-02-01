package com.example.cowmjucraft.domain.recruit.controller.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.request.AddQuestionAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.request.FormCopyAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.request.FormCreateAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.request.FormQuestionUpdateAdminRequest;
import com.example.cowmjucraft.domain.recruit.dto.admin.response.*;
import com.example.cowmjucraft.domain.recruit.service.admin.FormAdminService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/admin")
@RestController
public class FormAdminController implements FormAdminControllerDocs {

    private final FormAdminService formAdminService;

    public FormAdminController(FormAdminService formAdminService) {
        this.formAdminService = formAdminService;
    }

    @Override
    @PostMapping("/forms")
    public ApiResult<FormCreateAdminResponse> createForm(
            @RequestBody FormCreateAdminRequest request
    ) {
        return ApiResult.success(
                SuccessType.CREATED,
                formAdminService.createForm(request)
        );
    }

    @Override
    @PutMapping("/forms/{formId}/open")
    public ApiResult<?> openForm(@PathVariable Long formId) {
        formAdminService.openForm(formId);
        return ApiResult.success(SuccessType.SUCCESS);
    }

    @Override
    @PutMapping("/forms/{formId}/close")
    public ApiResult<?> closeForm(@PathVariable Long formId) {
        formAdminService.closeForm(formId);
        return ApiResult.success(SuccessType.SUCCESS);
    }

    @Override
    @PostMapping("/forms/{formId}/questions")
    public ApiResult<AddQuestionAdminResponse> addQuestion(
            @PathVariable Long formId,
            @RequestBody AddQuestionAdminRequest request
    ) {
        return ApiResult.success(
                SuccessType.CREATED,
                formAdminService.addQuestion(formId, request)
        );
    }

    @Override
    @GetMapping("/forms")
    public ApiResult<List<FormListAdminResponse>> getForms() {
        return ApiResult.success(
                SuccessType.SUCCESS,
                formAdminService.getForms()
        );
    }

    @Override
    @GetMapping("/forms/{formId}")
    public ApiResult<FormDetailAdminResponse> getForm(@PathVariable Long formId) {
        return ApiResult.success(
                SuccessType.SUCCESS,
                formAdminService.getForm(formId)
        );
    }

    @Override
    @GetMapping("/forms/{formId}/questions")
    public ApiResult<List<FormQuestionListAdminResponse>> getFormQuestions(
            @PathVariable Long formId
    ) {
        return ApiResult.success(
                SuccessType.SUCCESS,
                formAdminService.getFormQuestions(formId)
        );
    }

    @Override
    @DeleteMapping("/forms/{formId}/questions/{formQuestionId}")
    public ApiResult<?> deleteFormQuestion(
            @PathVariable Long formId,
            @PathVariable Long formQuestionId
    ) {
        formAdminService.deleteFormQuestion(formId, formQuestionId);
        return ApiResult.success(SuccessType.SUCCESS);
    }

    @Override
    @PutMapping("/forms/{formId}/questions/{formQuestionId}")
    public ApiResult<?> updateFormQuestion(
            @PathVariable Long formId,
            @PathVariable Long formQuestionId,
            @RequestBody FormQuestionUpdateAdminRequest request
    ) {
        formAdminService.updateFormQuestion(formId, formQuestionId, request);
        return ApiResult.success(SuccessType.SUCCESS);
    }

    @Override
    @PostMapping("/forms/{targetFormId}/copy")
    public ApiResult<FormCopyAdminResponse> copyFormQuestionsOverwrite(
            @PathVariable Long targetFormId,
            @RequestBody FormCopyAdminRequest request
    ) {
        return ApiResult.success(
                SuccessType.CREATED,
                formAdminService.copyFormQuestionsOverwrite(targetFormId, request)
        );
    }
}
