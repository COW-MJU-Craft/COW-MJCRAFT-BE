package com.example.cowmjucraft.domain.item.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "물품 presign-put 응답")
public record AdminItemPresignPutBatchResponseDto(

        @Schema(
                description = "발급된 presign-put 항목 목록",
                example = """
                        [
                          {
                            \"fileName\": \"thumbnail-1.png\",
                            \"key\": \"uploads/items/1/thumbnail/uuid-thumbnail-1.png\",
                            \"uploadUrl\": \"https://bucket.s3.amazonaws.com/...\",
                            \"expiresInSeconds\": 300
                          },
                          {
                            \"fileName\": \"thumbnail-2.png\",
                            \"key\": \"uploads/items/1/thumbnail/uuid-thumbnail-2.png\",
                            \"uploadUrl\": \"https://bucket.s3.amazonaws.com/...\",
                            \"expiresInSeconds\": 300
                          }
                        ]
                        """
        )
        List<ItemDto> items
) {
    public record ItemDto(
            @Schema(description = "요청 파일명", example = "thumbnail.png")
            String fileName,

            @Schema(description = "S3 key", example = "uploads/items/1/thumbnail/uuid-thumbnail.png")
            String key,

            @Schema(description = "Presigned PUT URL", example = "https://bucket.s3.amazonaws.com/...")
            String uploadUrl,

            @Schema(description = "URL 만료 시간(초)", example = "300")
            int expiresInSeconds
    ) {
    }
}
