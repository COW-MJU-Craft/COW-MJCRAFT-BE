package com.example.cowmjucraft.domain.feedback.service;

import com.example.cowmjucraft.domain.feedback.entity.Feedback;
import com.example.cowmjucraft.domain.feedback.exception.FeedbackErrorType;
import com.example.cowmjucraft.domain.feedback.exception.FeedbackException;
import com.example.cowmjucraft.domain.feedback.repository.FeedbackRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FeedbackAdminService {

    private final FeedbackRepository feedbackRepository;

    @Transactional
    public void deleteFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId).orElseThrow(() -> new FeedbackException(FeedbackErrorType.FEEDBACK_NOT_FOUND));

        feedbackRepository.delete(feedback);
    }
}
