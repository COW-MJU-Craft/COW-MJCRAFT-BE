package com.example.cowmjucraft.domain.media.controller.admin;

import com.example.cowmjucraft.domain.media.dto.request.AdminMediaPresignRequestDto;
import com.example.cowmjucraft.domain.media.dto.response.AdminMediaPresignResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin Media", description = "관리자 미디어 업로드/관리 API")
@SecurityRequirement(name = "bearerAuth")
public interface AdminMediaControllerDocs {

    @Operation(
            summary = "업로드용 presign 발급",
            description = "presign 발급 → S3에 직접 PUT 업로드 흐름을 위한 첫 단계입니다. presign은 Media 엔티티를 생성하지 않으며, 업로드 후 Media 생성 및 도메인 연결은 추후 도메인 API에서 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발급 성공", content = @Content(schema = @Schema(implementation = AdminMediaPresignResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 아님")
    })
    AdminMediaPresignResponseDto presign(@Valid @RequestBody AdminMediaPresignRequestDto request);

    @Operation(
            summary = "미디어 삭제",
            description = "DB 상태를 DELETED로 전환하고 S3 객체를 삭제합니다. 이미 삭제된 경우 멱등 처리됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 완료"),
            @ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 아님"),
            @ApiResponse(responseCode = "404", description = "대상 미디어 없음")
    })
    void delete(@PathVariable Long mediaId);
}
