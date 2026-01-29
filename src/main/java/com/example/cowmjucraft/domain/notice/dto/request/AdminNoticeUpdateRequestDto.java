package com.example.cowmjucraft.domain.notice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "공지사항 수정 요청")
public record AdminNoticeUpdateRequestDto(

        @NotBlank
        @Size(max = 100)
        @Schema(description = "공지 제목", example = "설 연휴 배송 일정 안내")
        String title,

        @NotBlank
        @Schema(description = "공지 내용", example = "설 연휴 기간 동안 배송이 중단됩니다.")
        String content,

        @Schema(description = "공지 이미지 S3 object key 목록", example = "[\"uploads/notices/images/uuid-notice.png\"]")
        List<@NotBlank @Size(max = 255) String> imageKeys
) {
}
