package com.example.cowmjucraft.domain.media.dto.request;

import com.example.cowmjucraft.domain.media.policy.MediaUsageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Presign PUT batch 요청")
public record MediaPresignPutRequestDto(
        @Schema(description = "업로드 디렉토리(없으면 usageType 기반 기본 prefix)", example = "uploads/intro")
        String directory,
        @Schema(description = "업로드 파일 목록")
        @NotNull
        @NotEmpty
        @Valid
        List<FileDto> files
) {
    @Schema(description = "파일 업로드 정보")
    public record FileDto(
            @Schema(description = "원본 파일명", example = "test.png")
            @NotBlank
            String fileName,
            @Schema(description = "업로드할 파일의 Content-Type", example = "image/png")
            @NotBlank
            String contentType,
            @Schema(description = "미디어 사용처 유형", example = "INTRO")
            @NotNull
            MediaUsageType usageType
    ) {
    }
}
