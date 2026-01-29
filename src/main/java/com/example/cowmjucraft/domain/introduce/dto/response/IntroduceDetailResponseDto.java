package com.example.cowmjucraft.domain.introduce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "소개 상세 응답 DTO")
public record IntroduceDetailResponseDto(
        @Schema(
                description = "소개 섹션 목록 (type 필드 포함)",
                example = """
                        [
                          {
                            "type": "PURPOSE",
                            "title": "우리가 하는 일",
                            "description": "명지공방은 학생들이 직접 기획하고 제작하는 굿즈 프로젝트를 운영합니다."
                          }
                        ]
                        """
        )
        List<Map<String, Object>> sections
) {
}
