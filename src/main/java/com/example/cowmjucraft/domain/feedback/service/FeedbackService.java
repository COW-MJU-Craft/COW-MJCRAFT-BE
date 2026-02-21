package com.example.cowmjucraft.domain.feedback.service;

import com.example.cowmjucraft.domain.feedback.dto.request.FeedbackAdminUpdateRequestDto;
import com.example.cowmjucraft.domain.feedback.dto.request.FeedbackRequestDto;
import com.example.cowmjucraft.domain.feedback.dto.response.FeedbackAdminResponseDto;
import com.example.cowmjucraft.domain.feedback.entity.Feedback;
import com.example.cowmjucraft.domain.feedback.exception.FeedbackErrorType;
import com.example.cowmjucraft.domain.feedback.exception.FeedbackException;
import com.example.cowmjucraft.domain.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Transactional
    public void createFeedback(FeedbackRequestDto request) {
        FeedbackRequestDto safeRequest =
                Objects.requireNonNull(request, "request must not be null");

        Feedback feedback = new Feedback(
                safeRequest.title(),
                safeRequest.content()
        );

        feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public List<FeedbackAdminResponseDto> getFeedbacks() {
        return feedbackRepository.findAllByOrderByIdDesc()
                .stream()
                .map(FeedbackAdminResponseDto::from)
                .toList();
    }

    @Transactional
    public void updateFeedback(Long id, FeedbackAdminUpdateRequestDto request) {
        FeedbackAdminUpdateRequestDto safeRequest =
                Objects.requireNonNull(request, "request must not be null");

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new FeedbackException(FeedbackErrorType.FEEDBACK_NOT_FOUND));

        feedback.updateAnswer(safeRequest.answer(), safeRequest.status());
    }
}
