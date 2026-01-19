package com.example.cowmjucraft.domain.media.dto.response;

import java.time.LocalDateTime;

import com.example.cowmjucraft.domain.media.entity.Media;
import com.example.cowmjucraft.domain.media.entity.MediaUsageType;
import com.example.cowmjucraft.domain.media.entity.MediaVisibility;
import io.swagger.v3.oas.annotations.media.Schema;

public record MediaMetadataResponseDto(
        @Schema(description = "미디어 ID", example = "1")
        Long mediaId,

        @Schema(description = "원본 파일명", example = "intro-banner.png")
        String originalFileName,

        @Schema(description = "Content-Type", example = "image/png")
        String contentType,

        @Schema(description = "미디어 사용처 유형", example = "INTRO")
        MediaUsageType usageType,

        @Schema(description = "공개 범위", example = "PUBLIC")
        MediaVisibility visibility,

        @Schema(description = "생성 시각", example = "2024-01-01T12:00:00")
        LocalDateTime createdAt,

        @Schema(description = "수정 시각", example = "2024-01-02T12:00:00")
        LocalDateTime updatedAt
) {
    public static MediaMetadataResponseDto from(Media media) {
        return new MediaMetadataResponseDto(
                media.getId(),
                media.getOriginalFileName(),
                media.getContentType(),
                media.getUsageType(),
                media.getVisibility(),
                media.getCreatedAt(),
                media.getUpdatedAt()
        );
    }
}
