package com.example.cowmjucraft.domain.feedback.repository;

import com.example.cowmjucraft.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByOrderByIdDesc();
}
