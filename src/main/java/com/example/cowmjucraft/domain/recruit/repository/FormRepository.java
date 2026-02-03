package com.example.cowmjucraft.domain.recruit.repository;

import com.example.cowmjucraft.domain.recruit.entity.Form;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormRepository extends JpaRepository<Form, Long> {

    Form findFirstByOpenTrue();

    Form findTopByOrderByIdDesc();

    List<Form> findAllByOrderByIdDesc();
}
