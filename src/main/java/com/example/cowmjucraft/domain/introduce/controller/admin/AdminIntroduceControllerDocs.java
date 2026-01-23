package com.example.cowmjucraft.domain.introduce.controller.admin;

import com.example.cowmjucraft.domain.introduce.dto.request.AdminIntroduceUpsertRequest;
import com.example.cowmjucraft.domain.introduce.dto.response.AdminIntroduceResponse;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Introduce - Admin", description = "소개 관리자 API")
public interface AdminIntroduceControllerDocs {

    @Operation(
            summary = "소개 원본 조회",
            description = "관리자 편집용 원본 데이터를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    ApiResult<AdminIntroduceResponse> getIntroduce();

    @Operation(
            summary = "소개 원본 저장",
            description = "소개 데이터를 전체 덮어쓰기 방식으로 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ApiResult<AdminIntroduceResponse> upsertIntroduce(
            @Valid @RequestBody AdminIntroduceUpsertRequest request
    );
}
