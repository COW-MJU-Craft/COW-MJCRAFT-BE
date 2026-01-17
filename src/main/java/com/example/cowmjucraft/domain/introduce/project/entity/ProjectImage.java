package com.example.cowmjucraft.domain.introduce.project.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "project_images",
        indexes = {
                @Index(name = "idx_project_images_project_sort", columnList = "project_id, sortOrder"),
                @Index(name = "idx_project_images_project", columnList = "project_id")
        }
)
public class ProjectImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private Long mediaId;

    @Column(nullable = false)
    private int sortOrder;

    public ProjectImage(Project project, Long mediaId, int sortOrder) {
        this.project = project;
        this.mediaId = mediaId;
        this.sortOrder = sortOrder;
    }

}
