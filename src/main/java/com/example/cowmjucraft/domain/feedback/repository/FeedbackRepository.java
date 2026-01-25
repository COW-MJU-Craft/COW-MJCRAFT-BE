package com.example.cowmjucraft.domain.feedback.repository;

import com.example.cowmjucraft.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 리스트 한꺼번에 주면 나중에 피드백 데이터 무거워지니까 페이징 처리

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByOrderByIdDesc();
}
