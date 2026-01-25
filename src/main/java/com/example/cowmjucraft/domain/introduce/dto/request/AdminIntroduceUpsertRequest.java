package com.example.cowmjucraft.domain.introduce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@Schema(description = "소개 관리자 저장(전체 덮어쓰기) 요청 DTO")
public record AdminIntroduceUpsertRequest(

        @NotBlank
        @Schema(description = "제목", example = "명지공방 소개")
        String title,

        @Schema(description = "부제목", example = "우리의 방향과 비전", nullable = true)
        String subtitle,

        @Schema(description = "요약", example = "명지공방은 ...", nullable = true)
        String summary,

        @Schema(description = "메인 히어로 로고 S3 object key 목록", nullable = true)
        List<String> heroLogoKeys,

        @NotNull
        @NotEmpty
        @Schema(description = "소개 섹션 목록 (type 필드 포함)")
        List<Map<String, Object>> sections
) {
}
