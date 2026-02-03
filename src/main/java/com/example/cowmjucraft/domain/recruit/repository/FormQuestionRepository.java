package com.example.cowmjucraft.domain.recruit.repository;

import com.example.cowmjucraft.domain.recruit.entity.Form;
import com.example.cowmjucraft.domain.recruit.entity.FormQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FormQuestionRepository extends JpaRepository<FormQuestion, Long> {

    List<FormQuestion> findAllByFormOrderByQuestionOrderAsc(Form form);

    List<FormQuestion> findAllByForm(Form form);

    void deleteAllByForm(Form form);

    List<FormQuestion> findAllByIdInAndForm_Id(Collection<Long> ids, Long formId);

    boolean existsByFormAndQuestionOrder(Form form, int questionOrder);
}
