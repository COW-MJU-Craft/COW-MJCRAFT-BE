package com.example.cowmjucraft.domain.recruit.repository;

import com.example.cowmjucraft.domain.recruit.entity.Answer;
import com.example.cowmjucraft.domain.recruit.entity.Application;
import com.example.cowmjucraft.domain.recruit.entity.FormQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findAllByApplication(Application application);

    void deleteAllByApplication(Application application);

    Optional<Answer> findByApplicationAndFormQuestion(Application application, FormQuestion formQuestion);

}


