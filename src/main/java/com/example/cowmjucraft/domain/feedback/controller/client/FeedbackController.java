package com.example.cowmjucraft.domain.feedback.controller.client;

import com.example.cowmjucraft.domain.feedback.dto.request.FeedbackRequestDto;
import com.example.cowmjucraft.domain.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class FeedbackController implements FeedbackControllerDocs {

    private final FeedbackService feedbackService;

    @PostMapping("/feedback")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void createFeedback(@Valid @RequestBody FeedbackRequestDto request) {
        feedbackService.createFeedback(request);
    }
}
