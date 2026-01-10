package com.example.cowmjucraft.domain.media.dto.response;

public record MediaPresignUrlResponseDto(
        @io.swagger.v3.oas.annotations.media.Schema(description = "S3 GET 다운로드/미리보기용 presigned URL", example = "https://bucket.s3.amazonaws.com/...")
        String downloadUrl
) {
}
