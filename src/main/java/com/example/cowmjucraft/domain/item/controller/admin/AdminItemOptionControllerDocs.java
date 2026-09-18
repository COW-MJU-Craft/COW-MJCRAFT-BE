package com.example.cowmjucraft.domain.item.controller.admin;

import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionGroupCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionGroupUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionValueCreateRequestDto;
import com.example.cowmjucraft.domain.item.dto.request.AdminItemOptionValueUpdateRequestDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemOptionGroupResponseDto;
import com.example.cowmjucraft.domain.item.dto.response.AdminItemOptionValueResponseDto;
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
import org.springframework.http.ResponseEntity;

@Tag(name = "Item Option - Admin", description = "상품 옵션 관리자 API")
public interface AdminItemOptionControllerDocs {

    @Operation(
            summary = "상품 옵션 그룹 목록 조회 (관리자)",
            description = "상품의 옵션 그룹과 각 그룹의 옵션 값을 정렬 순서대로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음")
    })
    ResponseEntity<ApiResult<List<AdminItemOptionGroupResponseDto>>> getOptionGroups(
            @Parameter(description = "상품 ID", example = "1")
            Long itemId
    );

    @Operation(
            summary = "상품 옵션 그룹 생성",
            description = "상품에 새 옵션 그룹(예: 색상)을 추가합니다."
    )
    @RequestBody(
            required = true,
            description = "옵션 그룹 생성 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminItemOptionGroupCreateRequestDto.class),
                    examples = @ExampleObject(
                            name = "option-group-create-request",
                            value = """
                                    {
                                      "name": "색상",
                                      "required": true,
                                      "sortOrder": 0
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 정렬 순서"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ResponseEntity<ApiResult<AdminItemOptionGroupResponseDto>> createOptionGroup(
            @Parameter(description = "상품 ID", example = "1")
            Long itemId,
            @Valid AdminItemOptionGroupCreateRequestDto request
    );

    @Operation(
            summary = "상품 옵션 그룹 수정",
            description = "옵션 그룹명, 필수 여부, 정렬 순서를 수정합니다."
    )
    @RequestBody(
            required = true,
            description = "옵션 그룹 수정 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminItemOptionGroupUpdateRequestDto.class),
                    examples = @ExampleObject(
                            name = "option-group-update-request",
                            value = """
                                    {
                                      "name": "색상",
                                      "required": true,
                                      "sortOrder": 0
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
            @ApiResponse(responseCode = "409", description = "해당 상품의 옵션 그룹이 아니거나, 이미 사용 중인 정렬 순서"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ResponseEntity<ApiResult<AdminItemOptionGroupResponseDto>> updateOptionGroup(
            @Parameter(description = "상품 ID", example = "1")
            Long itemId,
            @Parameter(description = "옵션 그룹 ID", example = "1")
            Long groupId,
            @Valid AdminItemOptionGroupUpdateRequestDto request
    );

    @Operation(
            summary = "상품 옵션 그룹 삭제",
            description = "옵션 그룹과 그 하위 옵션 값을 모두 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "해당 상품의 옵션 그룹이 아님")
    })
    ResponseEntity<ApiResult<Void>> deleteOptionGroup(
            @Parameter(description = "상품 ID", example = "1")
            Long itemId,
            @Parameter(description = "옵션 그룹 ID", example = "1")
            Long groupId
    );

    @Operation(
            summary = "상품 옵션 값 생성",
            description = "옵션 그룹에 새 옵션 값(예: 블랙)을 추가합니다."
    )
    @RequestBody(
            required = true,
            description = "옵션 값 생성 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminItemOptionValueCreateRequestDto.class),
                    examples = @ExampleObject(
                            name = "option-value-create-request",
                            value = """
                                    {
                                      "name": "블랙",
                                      "additionalPrice": 500,
                                      "stockQty": 10,
                                      "sortOrder": 0
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "해당 상품의 옵션 그룹이 아니거나, 이미 사용 중인 정렬 순서"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ResponseEntity<ApiResult<AdminItemOptionValueResponseDto>> createOptionValue(
            @Parameter(description = "상품 ID", example = "1")
            Long itemId,
            @Parameter(description = "옵션 그룹 ID", example = "1")
            Long groupId,
            @Valid AdminItemOptionValueCreateRequestDto request
    );

    @Operation(
            summary = "상품 옵션 값 수정",
            description = "옵션 값 이름, 추가 금액, 재고, 정렬 순서를 수정합니다."
    )
    @RequestBody(
            required = true,
            description = "옵션 값 수정 요청",
            content = @Content(
                    schema = @Schema(implementation = AdminItemOptionValueUpdateRequestDto.class),
                    examples = @ExampleObject(
                            name = "option-value-update-request",
                            value = """
                                    {
                                      "name": "블랙",
                                      "additionalPrice": 500,
                                      "stockQty": 8,
                                      "sortOrder": 0
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
            @ApiResponse(responseCode = "409", description = "해당 옵션 그룹의 값이 아니거나, 이미 사용 중인 정렬 순서"),
            @ApiResponse(responseCode = "422", description = "요청 값 검증 실패")
    })
    ResponseEntity<ApiResult<AdminItemOptionValueResponseDto>> updateOptionValue(
            @Parameter(description = "상품 ID", example = "1")
            Long itemId,
            @Parameter(description = "옵션 그룹 ID", example = "1")
            Long groupId,
            @Parameter(description = "옵션 값 ID", example = "1")
            Long valueId,
            @Valid AdminItemOptionValueUpdateRequestDto request
    );

    @Operation(
            summary = "상품 옵션 값 삭제",
            description = "단일 옵션 값을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(responseCode = "404", description = "요청한 리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "해당 옵션 그룹의 값이 아님")
    })
    ResponseEntity<ApiResult<Void>> deleteOptionValue(
            @Parameter(description = "상품 ID", example = "1")
            Long itemId,
            @Parameter(description = "옵션 그룹 ID", example = "1")
            Long groupId,
            @Parameter(description = "옵션 값 ID", example = "1")
            Long valueId
    );
}
