package com.example.cowmjucraft.domain.recruit.repository;

import com.example.cowmjucraft.domain.recruit.entity.RecruitSettings;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecruitSettingsRepository extends JpaRepository<RecruitSettings, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rs from RecruitSettings rs where rs.id = :id")
    Optional<RecruitSettings> findByIdForUpdate(@Param("id") Long id);
}
