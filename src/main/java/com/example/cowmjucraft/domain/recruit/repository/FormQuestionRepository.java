package com.example.cowmjucraft.domain.recruit.repository;

import com.example.cowmjucraft.domain.recruit.entity.Form;
import com.example.cowmjucraft.domain.recruit.entity.FormQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FormQuestionRepository extends JpaRepository<FormQuestion, Long> {

    List<FormQuestion> findAllByFormOrderByQuestionOrderAsc(Form form);

    List<FormQuestion> findAllByForm(Form form);

    void deleteAllByForm(Form form);

    List<FormQuestion> findAllByIdInAndForm_Id(Collection<Long> ids, Long formId);

    boolean existsByFormAndQuestionOrder(Form form, int questionOrder);

    @Query("select fq.question.id from FormQuestion fq where fq.form.id = :formId")
    List<Long> findQuestionIdsByFormId(@Param("formId") Long formId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from FormQuestion fq where fq.form.id = :formId")
    void deleteAllByFormId(@Param("formId") Long formId);
}
