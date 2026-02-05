package com.example.cowmjucraft.domain.project.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    @ElementCollection
    @CollectionTable(name = "project_images", joinColumns = @JoinColumn(name = "project_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "image_key", length = 255, nullable = false)
    private List<String> imageKeys = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate deadlineDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectCategory category;

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
            List<String> imageKeys,
            LocalDate deadlineDate,
            ProjectStatus status,
            ProjectCategory category
    ) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.thumbnailKey = thumbnailKey;
        replaceImageKeys(imageKeys);
        this.deadlineDate = deadlineDate;
        this.status = status;
        this.category = category == null ? ProjectCategory.GOODS : category;
    }

    public void updateBasic(
            String title,
            String summary,
            String description,
            String thumbnailKey,
            List<String> imageKeys,
            LocalDate deadlineDate,
            ProjectStatus status,
            ProjectCategory category
    ) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.thumbnailKey = thumbnailKey;
        replaceImageKeys(imageKeys);
        this.deadlineDate = deadlineDate;
        this.status = status;
        if (category != null) {
            this.category = category;
        }
    }

    public void applyPinned(boolean pinned, Integer pinnedOrder) {
        this.pinned = pinned;
        this.pinnedOrder = pinnedOrder;
    }

    public void applyManualOrder(Integer manualOrder) {
        this.manualOrder = manualOrder;
    }

    private void replaceImageKeys(List<String> imageKeys) {
        this.imageKeys.clear();
        if (imageKeys != null && !imageKeys.isEmpty()) {
            this.imageKeys.addAll(imageKeys);
        }
    }
}
