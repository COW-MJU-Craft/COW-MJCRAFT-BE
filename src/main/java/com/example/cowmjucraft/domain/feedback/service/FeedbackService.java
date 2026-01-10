package com.example.cowmjucraft.domain.feedback.service;

import com.example.cowmjucraft.domain.feedback.dto.request.FeedbackAdminUpdateRequestDto;
import com.example.cowmjucraft.domain.feedback.dto.request.FeedbackRequestDto;
import com.example.cowmjucraft.domain.feedback.dto.response.FeedbackAdminResponseDto;
import com.example.cowmjucraft.domain.feedback.entity.Feedback;
import com.example.cowmjucraft.domain.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "feedback not found"));

        feedback.updateAnswer(safeRequest.answer(), safeRequest.status());
    }
}
