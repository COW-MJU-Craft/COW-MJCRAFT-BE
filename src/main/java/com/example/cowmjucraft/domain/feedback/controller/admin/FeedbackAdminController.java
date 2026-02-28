package com.example.cowmjucraft.domain.feedback.controller.admin;

import com.example.cowmjucraft.domain.feedback.dto.request.FeedbackAdminUpdateRequestDto;
import com.example.cowmjucraft.domain.feedback.dto.response.FeedbackAdminResponseDto;
import com.example.cowmjucraft.domain.feedback.service.FeedbackAdminService;
import com.example.cowmjucraft.domain.feedback.service.FeedbackService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class FeedbackAdminController implements FeedbackAdminControllerDocs {

    private final FeedbackService feedbackService;
    private final FeedbackAdminService feedbackAdminService;

    @GetMapping("/feedback")
    @Override
    public ResponseEntity<ApiResult<List<FeedbackAdminResponseDto>>> getFeedbacks() {
        return ApiResponse.of(SuccessType.SUCCESS, feedbackService.getFeedbacks());
    }

    @PutMapping("/feedback/{id}")
    @Override
    public ResponseEntity<ApiResult<Void>> updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackAdminUpdateRequestDto request
    ) {
        feedbackService.updateFeedback(id, request);
        return ApiResponse.of(SuccessType.SUCCESS);
    }

    @DeleteMapping("/feedback/{feedbackId}")
    @Override
    public ResponseEntity<ApiResult<Void>> deleteFeedback(@PathVariable Long feedbackId) {
        feedbackAdminService.deleteFeedback(feedbackId);

        return ApiResponse.of(SuccessType.SUCCESS);
    }
}
