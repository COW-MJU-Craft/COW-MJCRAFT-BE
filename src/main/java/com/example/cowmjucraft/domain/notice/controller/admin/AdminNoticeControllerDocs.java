package com.example.cowmjucraft.domain.notice.controller.admin;

import com.example.cowmjucraft.domain.notice.dto.request.AdminNoticeCreateRequestDto;
import com.example.cowmjucraft.domain.notice.dto.request.AdminNoticePresignPutRequestDto;
import com.example.cowmjucraft.domain.notice.dto.request.AdminNoticeUpdateRequestDto;
import com.example.cowmjucraft.domain.notice.dto.response.AdminNoticePresignPutResponseDto;
import com.example.cowmjucraft.domain.notice.dto.response.NoticeDetailResponseDto;
import com.example.cowmjucraft.domain.notice.dto.response.NoticeSummaryResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Notice - Admin", description = "공지사항 관리자 API")
public interface AdminNoticeControllerDocs {

    @Operation(summary = "공지사항 생성", description = "공지사항을 생성합니다.")
    @RequestBody(
            required = true,
            description = "공지사항 생성 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminNoticeCreateRequestDto.class),
                    examples = @ExampleObject(
                            name = "notice-create-request",
                            value = """
                                    {
                                      "title": "설 연휴 배송 일정 안내",
                                      "content": "설 연휴 기간 동안 배송이 중단됩니다.",
                                      "imageKeys": ["uploads/notices/images/uuid-notice.png"]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "notice-create-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 201,
                                              "message": "성공적으로 생성하였습니다.",
                                              "data": {
                                                "id": 1,
                                                "title": "설 연휴 배송 일정 안내",
                                                "content": "설 연휴 기간 동안 배송이 중단됩니다.",
                                                "imageKeys": ["uploads/notices/images/uuid-notice.png"],
                                                "createdAt": "2026-01-20T10:15:30",
                                                "updatedAt": "2026-01-20T10:15:30"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ApiResult<NoticeDetailResponseDto> createNotice(
            @Valid AdminNoticeCreateRequestDto request
    );

    @Operation(summary = "공지사항 이미지 presign-put 발급", description = "공지사항 이미지 업로드용 presigned PUT URL을 발급합니다.")
    @RequestBody(
            required = true,
            description = "presign-put 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminNoticePresignPutRequestDto.class),
                    examples = @ExampleObject(
                            name = "notice-image-presign-request",
                            value = """
                                    {
                                      "fileName": "notice.png",
                                      "contentType": "image/png"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "notice-image-presign-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                              "data": {
                                                "key": "uploads/notices/images/uuid-notice.png",
                                                "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                                                "expiresInSeconds": 300
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ApiResult<AdminNoticePresignPutResponseDto> presignImage(
            @Valid AdminNoticePresignPutRequestDto request
    );

    @Operation(summary = "공지사항 수정", description = "공지사항을 수정합니다.")
    @RequestBody(
            required = true,
            description = "공지사항 수정 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminNoticeUpdateRequestDto.class),
                    examples = @ExampleObject(
                            name = "notice-update-request",
                            value = """
                                    {
                                      "title": "설 연휴 배송 일정 안내 (수정)",
                                      "content": "설 연휴 기간 동안 배송이 중단됩니다.",
                                      "imageKeys": ["uploads/notices/images/uuid-notice.png"]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ApiResult<NoticeDetailResponseDto> updateNotice(
            @Parameter(description = "공지 ID", example = "1")
            Long noticeId,
            @Valid AdminNoticeUpdateRequestDto request
    );

    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "notice-delete-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 204,
                                              "message": "요청에 성공하였습니다.",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음")
    })
    ApiResult<?> deleteNotice(
            @Parameter(description = "공지 ID", example = "1")
            Long noticeId
    );

    @Operation(summary = "공지사항 목록 조회", description = "공지사항 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            )
    })
    ApiResult<List<NoticeSummaryResponseDto>> getNotices();

    @Operation(summary = "공지사항 상세 조회", description = "공지사항 상세를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음")
    })
    ApiResult<NoticeDetailResponseDto> getNotice(
            @Parameter(description = "공지 ID", example = "1")
            Long noticeId
    );
}
