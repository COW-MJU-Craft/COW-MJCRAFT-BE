package com.example.cowmjucraft.domain.item.controller.admin;

import com.example.cowmjucraft.domain.item.dto.request.AdminItemImageCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemImageOrderPatchRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemPresignPutBatchRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminProjectItemCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminProjectItemUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemImageOrderPatchResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemPresignPutBatchResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminProjectItemDetailResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminProjectItemResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemImageResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemJournalPresignGetResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Item - Admin", description = "물품 관리자 API")
public interface AdminItemControllerDocs {

    @Operation(
            summary = "프로젝트 물품 목록 조회 (관리자)",
            description = "프로젝트 물품 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음")
    })
    ResponseEntity<ApiResult<List<AdminProjectItemResponseDto>>> getItems(
            @Parameter(description = "프로젝트 ID", example = "1")
            Long projectId
    );

    @Operation(
            summary = "물품 상세 조회 (관리자)",
            description = "물품 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음")
    })
    ResponseEntity<ApiResult<AdminProjectItemDetailResponseDto>> getItem(
            @Parameter(description = "물품 ID", example = "1")
            Long itemId
    );

    @Operation(
            summary = "프로젝트 물품 생성",
            description = """
                    프로젝트에 새로운 물품을 생성합니다.
                    JOURNAL 아이템 업로드는 2-step입니다.
                    Step1) /api/admin/projects/{projectId}/journals/presign-put 호출 → uploadUrl로 PUT 업로드
                    Step2) 생성 요청의 journalFileKey에 Step1의 key 저장
                    """
    )
    @RequestBody(
            required = true,
            description = "물품 생성 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminProjectItemCreateRequestDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "item-create-request",
                                    value = """
                                            {
                                              "name": "명지공방 머그컵",
                                              "summary": "캠퍼스 감성을 담은 데일리 머그컵",
                                              "description": "캠퍼스 감성을 담은 머그컵입니다.",
                                              "price": 12000,
                                              "saleType": "GROUPBUY",
                                              "itemType": "PHYSICAL",
                                              "status": "OPEN",
                                              "thumbnailKey": "uploads/items/1/thumbnail/uuid-thumbnail.png",
                                              "targetQty": 100,
                                              "fundedQty": 0
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "journal-item-create-request",
                                    value = """
                                            {
                                              "name": "2026 겨울 저널",
                                              "summary": "연말 회고 저널",
                                              "description": "2026 연말 회고용 디지털 저널입니다.",
                                              "price": 0,
                                              "saleType": "NORMAL",
                                              "itemType": "DIGITAL_JOURNAL",
                                              "status": "OPEN",
                                              "journalFileKey": "uploads/projects/1/journals/uuid-journal-2026.pdf"
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "item-create-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 201,
                                              "message": "성공적으로 생성하였습니다.",
                                              "data": {
                                                "id": 1,
                                                "projectId": 10,
                                                "name": "명지공방 머그컵",
                                                "summary": "캠퍼스 감성을 담은 데일리 머그컵",
                                                "description": "캠퍼스 감성을 담은 머그컵입니다.",
                                                "price": 12000,
                                                "saleType": "GROUPBUY",
                                                "status": "OPEN",
                                                "thumbnailKey": "uploads/items/1/thumbnail/uuid-thumbnail.png",
                                                "thumbnailUrl": "https://bucket.s3.amazonaws.com/uploads/items/1/thumbnail/uuid-thumbnail.png?X-Amz-Signature=...",
                                                "targetQty": 100,
                                                "fundedQty": 0,
                                                "achievementRate": 0.0,
                                                "remainingQty": 100
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ResponseEntity<ApiResult<AdminProjectItemResponseDto>> createItem(
            @Parameter(description = "프로젝트 ID", example = "1")
            Long projectId,
            @Valid AdminProjectItemCreateRequestDto request
    );

    @Operation(
            summary = "프로젝트 물품 수정",
            description = """
                    물품 정보를 수정합니다.
                    JOURNAL 아이템 업로드는 2-step입니다.
                    Step1) /api/admin/projects/{projectId}/journals/presign-put 호출 → uploadUrl로 PUT 업로드
                    Step2) 수정 요청의 journalFileKey에 Step1의 key 저장
                    """
    )
    @RequestBody(
            required = true,
            description = "물품 수정 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminProjectItemUpdateRequestDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "item-update-request",
                                    value = """
                                            {
                                              "name": "명지공방 머그컵",
                                              "summary": "캠퍼스 감성을 담은 데일리 머그컵",
                                              "description": "캠퍼스 감성을 담은 머그컵입니다.",
                                              "price": 11000,
                                              "saleType": "NORMAL",
                                              "itemType": "PHYSICAL",
                                              "status": "OPEN",
                                              "thumbnailKey": "uploads/items/1/thumbnail/uuid-thumbnail.png",
                                              "targetQty": 100,
                                              "fundedQty": 0
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "journal-item-update-request",
                                    value = """
                                            {
                                              "name": "2026 겨울 저널",
                                              "summary": "연말 회고 저널",
                                              "description": "2026 연말 회고용 디지털 저널입니다.",
                                              "price": 0,
                                              "saleType": "NORMAL",
                                              "itemType": "DIGITAL_JOURNAL",
                                              "status": "OPEN",
                                              "journalFileKey": "uploads/projects/1/journals/uuid-journal-2026.pdf"
                                            }
                                            """
                            )
                    }
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
    ResponseEntity<ApiResult<AdminProjectItemResponseDto>> updateItem(
            @Parameter(description = "물품 ID", example = "1")
            Long itemId,
            @Valid AdminProjectItemUpdateRequestDto request
    );

    @Operation(
            summary = "물품 썸네일 presign-put 발급",
            description = "물품 썸네일 업로드용 presigned PUT URL을 여러 건 발급합니다."
    )
    @RequestBody(
            required = true,
            description = "presign-put 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminItemPresignPutBatchRequestDto.class),
                    examples = @ExampleObject(
                            name = "item-thumbnail-presign-request",
                            value = """
                                    {
                                      "files": [
                                        { "fileName": "thumbnail-1.png", "contentType": "image/png" },
                                        { "fileName": "thumbnail-2.png", "contentType": "image/png" }
                                      ]
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
                                    name = "item-thumbnail-presign-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                              "data": {
                                                "items": [
                                                  {
                                                    "fileName": "thumbnail-1.png",
                                                    "key": "uploads/items/1/thumbnail/uuid-thumbnail-1.png",
                                                    "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                                                    "expiresInSeconds": 300
                                                  },
                                                  {
                                                    "fileName": "thumbnail-2.png",
                                                    "key": "uploads/items/1/thumbnail/uuid-thumbnail-2.png",
                                                    "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                                                    "expiresInSeconds": 300
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ResponseEntity<ApiResult<AdminItemPresignPutBatchResponseDto>> presignThumbnail(
            @Parameter(description = "물품 ID", example = "1")
            Long itemId,
            @Valid AdminItemPresignPutBatchRequestDto request
    );

    @Operation(
            summary = "물품 상세 이미지 presign-put 발급",
            description = "물품 상세 이미지 업로드용 presigned PUT URL을 여러 건 발급합니다."
    )
    @RequestBody(
            required = true,
            description = "presign-put 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminItemPresignPutBatchRequestDto.class),
                    examples = @ExampleObject(
                            name = "item-image-presign-request",
                            value = """
                                    {
                                      "files": [
                                        { "fileName": "detail-1.png", "contentType": "image/png" },
                                        { "fileName": "detail-2.png", "contentType": "image/png" }
                                      ]
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
                                    name = "item-image-presign-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                              "data": {
                                                "items": [
                                                  {
                                                    "fileName": "detail-1.png",
                                                    "key": "uploads/items/1/images/uuid-detail-1.png",
                                                    "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                                                    "expiresInSeconds": 300
                                                  },
                                                  {
                                                    "fileName": "detail-2.png",
                                                    "key": "uploads/items/1/images/uuid-detail-2.png",
                                                    "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                                                    "expiresInSeconds": 300
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ResponseEntity<ApiResult<AdminItemPresignPutBatchResponseDto>> presignImage(
            @Parameter(description = "물품 ID", example = "1")
            Long itemId,
            @Valid AdminItemPresignPutBatchRequestDto request
    );

    @Operation(
            summary = "저널 파일 presign-put 발급",
            description = """
                    저널 파일 업로드용 presigned PUT URL을 발급합니다.
                    Step1) presign-put으로 key, uploadUrl 획득
                    Step2) uploadUrl로 PUT 업로드
                    Step3) 아이템 생성/수정 요청의 journalFileKey에 Step1의 key 저장
                    """
    )
    @RequestBody(
            required = true,
            description = "presign-put 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminItemPresignPutBatchRequestDto.class),
                    examples = @ExampleObject(
                            name = "journal-presign-request",
                            value = """
                                    {
                                      "files": [
                                        { "fileName": "journal-2026-01.pdf", "contentType": "application/pdf" }
                                      ]
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
                                    name = "journal-presign-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "Presign URL 발급 완료",
                                              "data": {
                                                "items": [
                                                  {
                                                    "fileName": "journal-2026-01.pdf",
                                                    "key": "uploads/projects/1/journals/uuid-journal-2026-01.pdf",
                                                    "uploadUrl": "https://bucket.s3.amazonaws.com/...",
                                                    "expiresInSeconds": 300
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ResponseEntity<ApiResult<AdminItemPresignPutBatchResponseDto>> presignJournalFile(
            @Parameter(description = "프로젝트 ID", example = "1")
            Long projectId,
            @Valid AdminItemPresignPutBatchRequestDto request
    );

    @Operation(
            summary = "프로젝트 물품 삭제",
            description = "물품을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "item-delete-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "미디어 삭제 완료",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음")
    })
    ResponseEntity<ApiResult<Void>> deleteItem(
            @Parameter(description = "물품 ID", example = "1")
            Long itemId
    );

    @Operation(
            summary = "물품 썸네일 삭제",
            description = "물품의 대표 이미지(thumbnailKey)를 제거하고 S3에서도 파일을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "item-thumbnail-delete-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "미디어 삭제 완료",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음")
    })
    ResponseEntity<ApiResult<Void>> deleteThumbnail(
            @Parameter(description = "물품 ID", example = "1")
            Long itemId
    );

    @Operation(
            summary = "저널 파일 삭제 (관리자)",
            description = """
                    DIGITAL_JOURNAL 아이템의 journalFileKey에 해당하는 S3 파일을 삭제하고,
                    DB의 journalFileKey를 null로 clear 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "item-journal-delete-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "미디어 삭제 완료",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "422", description = "itemType != DIGITAL_JOURNAL 또는 journalFileKey 없음"),
            @ApiResponse(responseCode = "500", description = "S3 삭제 실패(내부 오류)")
    })
    ResponseEntity<ApiResult<Void>> deleteJournalFile(
            @Parameter(description = "물품 ID", example = "1")
            Long itemId
    );

    @Operation(
            summary = "물품 이미지 등록",
            description = "물품 상세 이미지 목록을 등록합니다."
    )
    @RequestBody(
            required = true,
            description = "이미지 등록 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminItemImageCreateRequestDto.class),
                    examples = @ExampleObject(
                            name = "item-image-create-request",
                            value = """
                                    {
                                      "images": [
                                        { "imageKey": "uploads/items/1/images/uuid-detail-01.png", "sortOrder": 0 },
                                        { "imageKey": "uploads/items/1/images/uuid-detail-02.png", "sortOrder": 1 }
                                      ]
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
                                    name = "item-image-create-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                                  "data": [
                                                {
                                                  "id": 5,
                                                  "imageKey": "uploads/items/1/images/uuid-detail-01.png",
                                                  "imageUrl": "https://bucket.s3.amazonaws.com/uploads/items/1/images/uuid-detail-01.png?X-Amz-Signature=...",
                                                  "sortOrder": 0
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "요청이 현재 상태와 충돌합니다."),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ResponseEntity<ApiResult<List<ProjectItemImageResponseDto>>> addImages(
            @Parameter(description = "물품 ID", example = "1")
            Long itemId,
            @Valid AdminItemImageCreateRequestDto request
    );

    @Operation(
            summary = "물품 이미지 정렬 변경",
            description = "물품 이미지 정렬 순서를 일괄 변경합니다."
    )
    @RequestBody(
            required = true,
            description = "정렬 변경 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminItemImageOrderPatchRequestDto.class),
                    examples = @ExampleObject(
                            name = "item-image-order-request",
                            value = """
                                    {
                                      "imageIds": [30, 12, 15]
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
            @ApiResponse(responseCode = "409", description = "요청이 현재 상태와 충돌합니다."),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ResponseEntity<ApiResult<AdminItemImageOrderPatchResponseDto>> patchImageOrder(
            @Parameter(description = "물품 ID", example = "1")
            Long itemId,
            @Valid AdminItemImageOrderPatchRequestDto request
    );

    @Operation(
            summary = "물품 이미지 삭제",
            description = "단일 물품 이미지를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "item-image-delete-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "미디어 삭제 완료",
                                              "data": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "요청이 현재 상태와 충돌합니다.")
    })
    ResponseEntity<ApiResult<Void>> deleteImage(
            @Parameter(description = "물품 ID", example = "1")
            Long itemId,
            @Parameter(description = "이미지 ID", example = "1")
            Long imageId
    );

    @Operation(
            summary = "저널 다운로드 presign-get 발급 (관리자)",
            description = """
                    DIGITAL_JOURNAL 아이템 다운로드용 presigned GET URL을 발급합니다.
                    downloadUrl은 Content-Disposition=attachment로 내려가며 브라우저 다운로드가 동작합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "admin-journal-presign-get-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "Presign URL 발급 완료",
                                              "data": {
                                                "downloadUrl": "https://bucket.s3.amazonaws.com/uploads/projects/1/journals/uuid-journal-2026.pdf?X-Amz-Signature=..."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ResponseEntity<ApiResult<ProjectItemJournalPresignGetResponseDto>> presignJournalDownloadForAdmin(
            @Parameter(description = "물품 ID", example = "1")
            Long itemId
    );
}
