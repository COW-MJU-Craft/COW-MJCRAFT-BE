package com.example.cowmjucraft.domain.feedback.controller.admin;

import com.example.cowmjucraft.domain.feedback.dto.request.FeedbackAdminUpdateRequestDto;
import com.example.cowmjucraft.domain.feedback.dto.response.FeedbackAdminResponseDto;
import com.example.cowmjucraft.domain.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class FeedbackAdminController implements FeedbackAdminControllerDocs {

    private final FeedbackService feedbackService;

    @GetMapping("/feedback")
    @Override
    public List<FeedbackAdminResponseDto> getFeedbacks() {
        return feedbackService.getFeedbacks();
    }

    @PutMapping("/feedback/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackAdminUpdateRequestDto request
    ) {
        feedbackService.updateFeedback(id, request);
    }
}
