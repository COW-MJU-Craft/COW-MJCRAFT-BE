package com.example.cowmjucraft.domain.item.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "물품 presign-put 배치 요청")
public record AdminItemPresignPutBatchRequestDto(

        @Valid
        @NotEmpty
        @Schema(
                description = "업로드 파일 목록",
                example = """
                        [
                          { \"fileName\": \"thumbnail-1.png\", \"contentType\": \"image/png\" },
                          { \"fileName\": \"thumbnail-2.png\", \"contentType\": \"image/png\" }
                        ]
                        """
        )
        List<FileDto> files
) {
    public record FileDto(
            @NotBlank
            @Schema(description = "원본 파일명", example = "thumbnail.png")
            String fileName,

            @NotBlank
            @Schema(description = "파일 Content-Type", example = "image/png")
            String contentType
    ) {
    }
}
