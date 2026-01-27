package com.example.cowmjucraft.domain.project.repository;

import com.example.cowmjucraft.domain.project.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
    select p from Project p
    order by
        case when p.pinned = true then 0 else 1 end,
        case when p.pinned = true and p.pinnedOrder is null then 1 else 0 end,
        case when p.pinned = true then p.pinnedOrder else null end,
        case when p.pinned = false then p.deadlineDate else null end,
        case when p.pinned = false and p.manualOrder is null then 1 else 0 end,
        case when p.pinned = false then p.manualOrder else null end,
        p.createdAt desc,
        p.id desc
""")
    List<Project> findAllOrderedForPublic();
}
