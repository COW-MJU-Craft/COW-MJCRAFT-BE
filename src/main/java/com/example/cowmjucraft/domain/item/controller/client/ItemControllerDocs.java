package com.example.cowmjucraft.domain.item.controller.client;

import com.example.cowmjucraft.domain.item.dto.response.ProjectItemDetailResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.ProjectItemListResponseDto;
import com.example.cowmjucraft.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Item - Public", description = "물품 공개 조회 API")
public interface ItemControllerDocs {

    @Operation(
            summary = "프로젝트 물품 목록 조회",
            description = "프로젝트에 연결된 물품 목록을 최신순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "items-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                              "data": [
                                                {
                                                  "id": 1,
                                                  "name": "명지공방 머그컵",
                                                  "price": 12000,
                                                  "saleType": "GROUPBUY",
                                                  "status": "OPEN",
                                                  "thumbnailKey": "uploads/items/thumbnail-001.png",
                                                  "targetQty": 100,
                                                  "fundedQty": 40,
                                                  "achievementRate": 40.0,
                                                  "remainingQty": 60
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    ApiResult<List<ProjectItemListResponseDto>> getProjectItems(
            @Parameter(description = "프로젝트 ID", example = "1")
            Long projectId
    );

    @Operation(
            summary = "물품 상세 조회",
            description = "물품 상세 정보 및 상세 이미지 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResult.class),
                            examples = @ExampleObject(
                                    name = "item-detail-response",
                                    value = """
                                            {
                                              "resultType": "SUCCESS",
                                              "httpStatusCode": 200,
                                              "message": "요청에 성공하였습니다.",
                                              "data": {
                                                "id": 1,
                                                "projectId": 10,
                                                "name": "명지공방 머그컵",
                                                "description": "캠퍼스 감성을 담은 머그컵입니다.",
                                                "price": 12000,
                                                "saleType": "GROUPBUY",
                                                "status": "OPEN",
                                                "thumbnailKey": "uploads/items/thumbnail-001.png",
                                                "images": [
                                                  {
                                                    "id": 5,
                                                    "imageKey": "uploads/items/detail-001.png",
                                                    "sortOrder": 0
                                                  }
                                                ],
                                                "targetQty": 100,
                                                "fundedQty": 40,
                                                "achievementRate": 40.0,
                                                "remainingQty": 60
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    ApiResult<ProjectItemDetailResponseDto> getItem(
            @Parameter(description = "물품 ID", example = "1")
            Long itemId
    );
}
