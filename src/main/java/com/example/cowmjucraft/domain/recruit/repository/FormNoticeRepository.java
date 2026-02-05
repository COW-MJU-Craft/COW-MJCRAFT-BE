package com.example.cowmjucraft.domain.recruit.repository;

import com.example.cowmjucraft.domain.recruit.entity.Form;
import com.example.cowmjucraft.domain.recruit.entity.FormNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FormNoticeRepository extends JpaRepository<FormNotice, Long> {
    List<FormNotice> findAllByForm(Form form);
    void deleteAllByForm(Form form);
}