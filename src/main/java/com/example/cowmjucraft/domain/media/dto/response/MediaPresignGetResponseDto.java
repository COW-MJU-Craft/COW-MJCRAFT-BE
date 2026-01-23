package com.example.cowmjucraft.domain.media.dto.response;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

public record MediaPresignGetResponseDto(
        @Schema(description = "S3 object key -> presigned GET URL 매핑", example = "{\"uploads/projects/uuid-a.png\":\"https://bucket.s3.amazonaws.com/...\"}")
        Map<String, String> urls
) {
}
