package com.example.cowmjucraft.domain.item.repository;

import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectItemRepository extends JpaRepository<ProjectItem, Long> {

    List<ProjectItem> findByProjectIdOrderByCreatedAtDescIdDesc(Long projectId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pi from ProjectItem pi where pi.id = :id")
    Optional<ProjectItem> findByIdForUpdate(@Param("id") Long id);
}
