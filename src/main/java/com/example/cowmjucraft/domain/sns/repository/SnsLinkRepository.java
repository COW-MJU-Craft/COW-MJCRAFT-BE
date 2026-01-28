package com.example.cowmjucraft.domain.sns.repository;

import com.example.cowmjucraft.domain.sns.entity.SnsLink;
import com.example.cowmjucraft.domain.sns.entity.SnsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SnsLinkRepository extends JpaRepository<SnsLink, Long> {

    Optional<SnsLink> findByType(SnsType type);
}
