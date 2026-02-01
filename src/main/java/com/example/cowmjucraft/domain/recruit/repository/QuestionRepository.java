package com.example.cowmjucraft.domain.recruit.repository;

import com.example.cowmjucraft.domain.recruit.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
