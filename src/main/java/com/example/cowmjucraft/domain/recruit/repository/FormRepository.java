package com.example.cowmjucraft.domain.recruit.repository;

import com.example.cowmjucraft.domain.recruit.entity.Form;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormRepository extends JpaRepository<Form, Long> {

    Form findFirstByOpenTrue();

    Form findTopByOrderByIdDesc();

    //TODO: 기존의 form 조회 ui 및 개수 설계 -> 더 효율적인 조회 방식 고민
    List<Form> findAllByOrderByIdDesc();
}
