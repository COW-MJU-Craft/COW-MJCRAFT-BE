package com.example.cowmjucraft.domain.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "프로젝트 presign-put 배치 응답")
public record AdminProjectPresignPutBatchResponseDto(

        @Schema(
                description = "발급된 presign-put 항목 목록",
                example = """
                        [
                          {
                            "fileName": "thumbnail-1.png",
                            "key": "uploads/projects/thumbnails/uuid-thumbnail-1.png",
                            "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                            "expiresInSeconds": 300
                          },
                          {
                            "fileName": "thumbnail-2.png",
                            "key": "uploads/projects/thumbnails/uuid-thumbnail-2.png",
                            "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                            "expiresInSeconds": 300
                          }
                        ]
                        """
        )
        List<ItemDto> items
) {
    public record ItemDto(
            @Schema(description = "요청 파일명", example = "thumbnail.png")
            String fileName,

            @Schema(description = "S3 key", example = "uploads/projects/thumbnails/uuid-thumbnail.png")
            String key,

            @Schema(description = "Presigned PUT URL", example = "https://bucket.s3.amazonaws.com/...")
            String uploadUrl,

            @Schema(description = "URL 만료 시간(초)", example = "300")
            int expiresInSeconds
    ) {
    }
}
