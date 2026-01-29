package com.example.cowmjucraft.domain.item.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "물품 이미지 presign-put 요청")
public record AdminItemPresignPutRequestDto(

        @NotBlank
        @Schema(description = "원본 파일명", example = "detail.png")
        String fileName,

        @NotBlank
        @Schema(description = "파일 Content-Type", example = "image/png")
        String contentType
) {
}
