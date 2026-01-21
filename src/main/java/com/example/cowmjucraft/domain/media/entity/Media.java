package com.example.cowmjucraft.domain.media.entity;

import com.example.cowmjucraft.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "media")
public class Media extends BaseTimeEntity {

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

    public boolean isDeleted() {
        return this.status == MediaStatus.DELETED;
    }

    public boolean isActive() {
        return this.status == MediaStatus.ACTIVE;
    }

    public boolean isPublic() {
        return this.visibility == MediaVisibility.PUBLIC;
    }

    public boolean isPubliclyVisible() {
        return isActive() && isPublic();
    }

}
