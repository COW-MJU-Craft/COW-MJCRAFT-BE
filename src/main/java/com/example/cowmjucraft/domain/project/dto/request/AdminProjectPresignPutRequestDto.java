package com.example.cowmjucraft.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "프로젝트 썸네일 presign-put 요청")
public record AdminProjectPresignPutRequestDto(

        @NotBlank
        @Schema(description = "원본 파일명", example = "thumbnail.png")
        String fileName,

        @NotBlank
        @Schema(description = "파일 Content-Type", example = "image/png")
        String contentType
) {
}
