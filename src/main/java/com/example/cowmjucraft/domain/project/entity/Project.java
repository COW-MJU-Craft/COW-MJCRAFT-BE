package com.example.cowmjucraft.domain.project.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "projects")
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 255, nullable = false)
    private String summary;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(length = 255, nullable = false)
    private String thumbnailKey;

    @Column(nullable = false)
    private LocalDate deadlineDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Column(nullable = false)
    private boolean pinned = false;

    @Column
    private Integer pinnedOrder;

    @Column
    private Integer manualOrder;

    public Project(
            String title,
            String summary,
            String description,
            String thumbnailKey,
            LocalDate deadlineDate,
            ProjectStatus status
    ) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.thumbnailKey = thumbnailKey;
        this.deadlineDate = deadlineDate;
        this.status = status;
    }

    public void updateBasic(
            String title,
            String summary,
            String description,
            String thumbnailKey,
            LocalDate deadlineDate,
            ProjectStatus status
    ) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.thumbnailKey = thumbnailKey;
        this.deadlineDate = deadlineDate;
        this.status = status;
    }

    public void applyPinned(boolean pinned, Integer pinnedOrder) {
        this.pinned = pinned;
        this.pinnedOrder = pinnedOrder;
    }

    public void applyManualOrder(Integer manualOrder) {
        this.manualOrder = manualOrder;
    }
}
