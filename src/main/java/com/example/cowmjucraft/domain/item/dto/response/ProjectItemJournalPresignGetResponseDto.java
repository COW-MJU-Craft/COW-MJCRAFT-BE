package com.example.cowmjucraft.domain.item.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "저널 다운로드 presign-get 응답")
public record ProjectItemJournalPresignGetResponseDto(

        @Schema(description = "저널 다운로드 URL", example = "https://bucket.s3.amazonaws.com/uploads/projects/1/journals/uuid-journal.pdf?X-Amz-Signature=...")
        String downloadUrl
) {
}
