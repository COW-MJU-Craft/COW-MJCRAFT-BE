package com.example.cowmjucraft.domain.recruit.repository;

import com.example.cowmjucraft.domain.recruit.entity.Application;
import com.example.cowmjucraft.domain.recruit.entity.Form;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByFormAndStudentId(Form form, String studentId);

    Optional<Application> findByFormAndStudentId(Form form, String studentId);

    List<Application> findAllByForm(Form form);

    boolean existsByForm(Form form);
}
