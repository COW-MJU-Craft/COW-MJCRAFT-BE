package com.example.cowmjucraft.domain.item.repository;

import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectItemRepository extends JpaRepository<ProjectItem, Long> {

    List<ProjectItem> findByProjectIdOrderByCreatedAtDescIdDesc(Long projectId);
}
