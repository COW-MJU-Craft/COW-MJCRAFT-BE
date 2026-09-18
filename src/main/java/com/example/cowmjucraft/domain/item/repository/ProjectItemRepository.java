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

    // soft delete된 상품은 client·admin 목록 모두에서 제외한다(deleted_at IS NULL).
    @Query("""
    select pi from ProjectItem pi
    where pi.project.id = :projectId and pi.deletedAt is null
    order by pi.createdAt desc, pi.id desc
""")
    List<ProjectItem> findByProjectIdOrderByCreatedAtDescIdDesc(@Param("projectId") Long projectId);

    // 삭제 stamp 대상 수집 등 내부 처리용 — 삭제된 항목까지 전부 반환한다.
    List<ProjectItem> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pi from ProjectItem pi where pi.id = :id")
    Optional<ProjectItem> findByIdForUpdate(@Param("id") Long id);
}
