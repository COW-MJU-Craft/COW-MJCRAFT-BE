package com.example.cowmjucraft.domain.item.controller.admin;

import com.example.cowmjucraft.domain.item.dto.request.AdminItemImageCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemImageOrderPatchRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminProjectItemCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminProjectItemUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemImageOrderPatchResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminProjectItemResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemImageResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
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
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Item - Admin", description = "물품 관리자 API")
public interface AdminItemControllerDocs {

    @Operation(
            summary = "프로젝트 물품 생성",
            description = "프로젝트에 새로운 물품을 생성합니다."
    )
    @RequestBody(
            required = true,
            description = "물품 생성 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminProjectItemCreateRequestDto.class),
                    examples = @ExampleObject(
                            name = "item-create-request",
                            value = """
                                    {
                                      "name": "명지공방 머그컵",
                                      "description": "캠퍼스 감성을 담은 머그컵입니다.",
                                      "price": 12000,
                                      "saleType": "GROUPBUY",
                                      "status": "OPEN",
                                      "thumbnailKey": "uploads/items/thumbnail-001.png",
                                      "targetQty": 100,
                                      "fundedQty": 0
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
                                                "description": "캠퍼스 감성을 담은 머그컵입니다.",
                                                "price": 12000,
                                                "saleType": "GROUPBUY",
                                                "status": "OPEN",
                                                "thumbnailKey": "uploads/items/thumbnail-001.png",
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
            @ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ApiResult<AdminProjectItemResponseDto> createItem(
            @Parameter(description = "프로젝트 ID", example = "1")
            Long projectId,
            @Valid AdminProjectItemCreateRequestDto request
    );

    @Operation(
            summary = "프로젝트 물품 수정",
            description = "물품 정보를 수정합니다."
    )
    @RequestBody(
            required = true,
            description = "물품 수정 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminProjectItemUpdateRequestDto.class),
                    examples = @ExampleObject(
                            name = "item-update-request",
                            value = """
                                    {
                                      "name": "명지공방 머그컵",
                                      "description": "캠퍼스 감성을 담은 머그컵입니다.",
                                      "price": 11000,
                                      "saleType": "NORMAL",
                                      "status": "OPEN",
                                      "thumbnailKey": "uploads/items/thumbnail-001.png",
                                      "targetQty": 100,
                                      "fundedQty": 0
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
            @ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ApiResult<AdminProjectItemResponseDto> updateItem(
            @Parameter(description = "물품 ID", example = "1")
            Long itemId,
            @Valid AdminProjectItemUpdateRequestDto request
    );

    @Operation(
            summary = "프로젝트 물품 삭제",
            description = "물품을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    ApiResult<?> deleteItem(
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
                                        { "imageKey": "uploads/items/detail-001.png", "sortOrder": 0 },
                                        { "imageKey": "uploads/items/detail-002.png", "sortOrder": 1 }
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
                                                  "imageKey": "uploads/items/detail-001.png",
                                                  "sortOrder": 0
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ApiResult<List<ProjectItemImageResponseDto>> addImages(
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
            @ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ApiResult<AdminItemImageOrderPatchResponseDto> patchImageOrder(
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
                    responseCode = "204",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    ApiResult<?> deleteImage(
            @Parameter(description = "물품 ID", example = "1")
            @PathVariable Long itemId,
            @Parameter(description = "이미지 ID", example = "1")
            @PathVariable Long imageId
    );
}