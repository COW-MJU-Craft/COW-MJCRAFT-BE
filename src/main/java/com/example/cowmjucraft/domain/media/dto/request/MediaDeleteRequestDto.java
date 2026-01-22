package com.example.cowmjucraft.domain.media.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record MediaDeleteRequestDto(
        @Schema(description = "삭제 대상 S3 object key 목록", example = "[\"uploads/projects/uuid-a.png\"]")
        @NotEmpty
        List<@NotBlank String> keys
) {
}
