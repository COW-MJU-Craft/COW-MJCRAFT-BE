package com.example.cowmjucraft.domain.project.repository;

import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Project p where p.id = :projectId")
    Optional<Project> findByIdForUpdate(@Param("projectId") Long projectId);

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

    @Query("""
    select p from Project p
    where p.status = :status
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
    List<Project> findAllByStatusOrderedForPublic(@Param("status") ProjectStatus status);

    @Query("select p from Project p where p.pinned = true")
    List<Project> findAllPinned();
}
