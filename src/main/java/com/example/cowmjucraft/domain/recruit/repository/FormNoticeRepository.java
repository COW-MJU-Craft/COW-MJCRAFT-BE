package com.example.cowmjucraft.domain.recruit.repository;

import com.example.cowmjucraft.domain.recruit.entity.Form;
import com.example.cowmjucraft.domain.recruit.entity.FormNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FormNoticeRepository extends JpaRepository<FormNotice, Long> {
    List<FormNotice> findAllByForm(Form form);
    void deleteAllByForm(Form form);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from FormNotice fn where fn.form.id = :formId")
    void deleteAllByFormId(@Param("formId") Long formId);
}