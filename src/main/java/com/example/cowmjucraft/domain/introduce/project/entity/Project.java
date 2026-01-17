package com.example.cowmjucraft.domain.introduce.project.entity;

import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private String title;

    @Column
    private String summary;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Long basePrice;

    @Column
    private Long thumbnailMediaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Column
    private String salesLink;

    @Column(nullable = false)
    private int sortOrder;

    @Column
    private LocalDateTime publishedAt;

    public Project(
            String title,
            String summary,
            String description,
            Long basePrice,
            String salesLink,
            int sortOrder,
            Long thumbnailMediaId
    ) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.basePrice = basePrice;
        this.salesLink = salesLink;
        this.sortOrder = sortOrder;
        this.thumbnailMediaId = thumbnailMediaId;
        this.status = ProjectStatus.DRAFT;
    }

    public void update(
            String title,
            String summary,
            String description,
            Long basePrice,
            String salesLink,
            int sortOrder,
            Long thumbnailMediaId
    ) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.basePrice = basePrice;
        this.salesLink = salesLink;
        this.sortOrder = sortOrder;
        this.thumbnailMediaId = thumbnailMediaId;
    }

    public void publish() {
        this.status = ProjectStatus.PUBLISHED;
        if (this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    public void archive() {
        this.status = ProjectStatus.ARCHIVED;
    }

}
