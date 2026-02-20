package com.example.cowmjucraft.domain.feedback.controller.client;

import com.example.cowmjucraft.domain.feedback.dto.request.FeedbackRequestDto;
import com.example.cowmjucraft.domain.feedback.service.FeedbackService;
import com.example.cowmjucraft.global.response.ApiResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.SuccessType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class FeedbackController implements FeedbackControllerDocs {

    private final FeedbackService feedbackService;

    @PostMapping("/feedback")
    @Override
    public ResponseEntity<ApiResult<Void>> createFeedback(@Valid @RequestBody FeedbackRequestDto request) {
        feedbackService.createFeedback(request);
        return ApiResponse.of(SuccessType.CREATED);
    }
}
