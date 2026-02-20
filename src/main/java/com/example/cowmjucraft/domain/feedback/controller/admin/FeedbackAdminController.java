package com.example.cowmjucraft.domain.feedback.controller.admin;

import com.example.cowmjucraft.domain.feedback.dto.request.FeedbackAdminUpdateRequestDto;
import com.example.cowmjucraft.domain.feedback.dto.response.FeedbackAdminResponseDto;
import com.example.cowmjucraft.domain.feedback.service.FeedbackService;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class FeedbackAdminController implements FeedbackAdminControllerDocs {

    private final FeedbackService feedbackService;

    @GetMapping("/feedback")
    @Override
    public ApiResult<List<FeedbackAdminResponseDto>> getFeedbacks() {
        return ApiResult.success(SuccessType.SUCCESS, feedbackService.getFeedbacks());
    }

    @PutMapping("/feedback/{id}")
    @Override
    public ApiResult<?> updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackAdminUpdateRequestDto request
    ) {
        feedbackService.updateFeedback(id, request);
        return ApiResult.success(SuccessType.SUCCESS);
    }
}
