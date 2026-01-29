package com.example.cowmjucraft.domain.introduce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@Schema(description = "소개 관리자 저장(전체 덮어쓰기) 요청 DTO")
public record AdminIntroduceUpsertRequestDto(

        @NotBlank
        @Schema(description = "제목", example = "명지공방 소개")
        String title,

        @Schema(description = "부제목", example = "우리의 방향과 비전", nullable = true)
        String subtitle,

        @Schema(description = "요약", example = "명지공방은 학생들이 직접 만드는 공방 브랜드입니다.", nullable = true)
        String summary,

        @Schema(
                description = "메인 히어로 로고 S3 object key 목록",
                example = "[\"uploads/introduce/hero-logos/uuid-logo-1.png\", \"uploads/introduce/hero-logos/uuid-logo-2.png\"]",
                nullable = true
        )
        List<String> heroLogoKeys,

        @NotNull
        @NotEmpty
        @Schema(
                description = """
                        소개 섹션 목록 (type 필드 포함)
                        
                        섹션 타입별 권장 필드:
                        - HEADER: title, subtitle, backgroundImageKey?
                        - PURPOSE: title, description
                        - CURRENT_LOGO: title, logoImageKey, description
                        - LOGO_HISTORY: title, histories[{year, imageKey, description}]
                        """,
                example = """
                        [
                          {
                            "type": "HEADER",
                            "title": "명지공방",
                            "subtitle": "학생들이 만드는 작은 브랜드",
                            "backgroundImageKey": "uploads/introduce/sections/uuid-hero-bg.png"
                          },
                          {
                            "type": "PURPOSE",
                            "title": "우리가 하는 일",
                            "description": "명지공방은 학생들이 직접 기획하고 제작하는 굿즈 프로젝트를 운영합니다."
                          },
                          {
                            "type": "CURRENT_LOGO",
                            "title": "현재 로고",
                            "logoImageKey": "uploads/introduce/sections/uuid-current-logo.png",
                            "description": "현재 로고는 2025년 리브랜딩을 통해 확정되었습니다."
                          },
                          {
                            "type": "LOGO_HISTORY",
                            "title": "로고 히스토리",
                            "histories": [
                              {
                                "year": "2023",
                                "imageKey": "uploads/introduce/sections/uuid-logo-2023.png",
                                "description": "초기 로고 버전"
                              },
                              {
                                "year": "2024",
                                "imageKey": "uploads/introduce/sections/uuid-logo-2024.png",
                                "description": "심볼 개선 및 색상 정리"
                              }
                            ]
                          }
                        ]
                        """
        )
        List<Map<String, Object>> sections
) {
}
