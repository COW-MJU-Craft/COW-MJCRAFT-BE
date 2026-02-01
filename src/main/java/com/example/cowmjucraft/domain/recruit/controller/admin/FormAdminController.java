package com.example.cowmjucraft.domain.recruit.controller.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.*;
import com.example.cowmjucraft.domain.recruit.service.admin.FormAdminService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
public class FormAdminController implements FormAdminControllerDocs {

    private final FormAdminService formAdminService;

    public FormAdminController(FormAdminService formAdminService) {
        this.formAdminService = formAdminService;
    }

    @Override
    @PostMapping("/admin/forms")
    public ApiResult<FormCreateAdminResponse> createForm(
            @RequestBody FormCreateAdminRequest request
    ) {
        return ApiResult.success(
                SuccessType.CREATED,
                formAdminService.createForm(request)
        );
    }

    @Override
    @PutMapping("/admin/forms/{formId}/open")
    public ApiResult<?> openForm(@PathVariable Long formId) {
        formAdminService.openForm(formId);
        return ApiResult.success(SuccessType.SUCCESS);
    }

    @Override
    @PutMapping("/admin/forms/{formId}/close")
    public ApiResult<?> closeForm(@PathVariable Long formId) {
        formAdminService.closeForm(formId);
        return ApiResult.success(SuccessType.SUCCESS);
    }

    @Override
    @PostMapping("/admin/forms/{formId}/questions")
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
    @GetMapping("/admin/forms")
    public ApiResult<List<FormListAdminResponse>> listForms() {
        return ApiResult.success(
                SuccessType.SUCCESS,
                formAdminService.listForms()
        );
    }

    @Override
    @GetMapping("/admin/forms/{formId}")
    public ApiResult<FormDetailAdminResponse> getForm(@PathVariable Long formId) {
        return ApiResult.success(
                SuccessType.SUCCESS,
                formAdminService.getForm(formId)
        );
    }

    @Override
    @GetMapping("/admin/forms/{formId}/questions")
    public ApiResult<List<FormQuestionListAdminResponse>> listFormQuestions(
            @PathVariable Long formId
    ) {
        return ApiResult.success(
                SuccessType.SUCCESS,
                formAdminService.listFormQuestions(formId)
        );
    }

    @Override
    @DeleteMapping("/admin/forms/{formId}/questions/{formQuestionId}")
    public ApiResult<?> deleteFormQuestion(
            @PathVariable Long formId,
            @PathVariable Long formQuestionId
    ) {
        formAdminService.deleteFormQuestion(formId, formQuestionId);
        return ApiResult.success(SuccessType.NO_CONTENT);
    }

    @Override
    @PutMapping("/admin/forms/{formId}/questions/{formQuestionId}")
    public ApiResult<?> updateFormQuestion(
            @PathVariable Long formId,
            @PathVariable Long formQuestionId,
            @RequestBody FormQuestionUpdateAdminRequest request
    ) {
        formAdminService.updateFormQuestion(formId, formQuestionId, request);
        return ApiResult.success(SuccessType.SUCCESS);
    }

    @Override
    @PostMapping("/admin/forms/{targetFormId}/copy")
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
