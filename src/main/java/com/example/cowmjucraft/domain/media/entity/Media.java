package com.example.cowmjucraft.domain.media.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "media")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "object_key", nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaUsageType usageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Media(
            String key,
            String originalFileName,
            String contentType,
            MediaUsageType usageType,
            MediaVisibility visibility
    ) {
        this.key = key;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.usageType = usageType;
        this.visibility = visibility;
        this.status = MediaStatus.PENDING;
    }

    public void activate() {
        this.status = MediaStatus.ACTIVE;
    }

    public void markDeleted() {
        this.status = MediaStatus.DELETED;
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
