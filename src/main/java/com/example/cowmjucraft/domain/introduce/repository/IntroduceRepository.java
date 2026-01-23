package com.example.cowmjucraft.domain.introduce.repository;

import com.example.cowmjucraft.domain.introduce.entity.Introduce;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntroduceRepository extends JpaRepository<Introduce, Long> {

    Optional<Introduce> findTopByOrderByIdAsc();
}
