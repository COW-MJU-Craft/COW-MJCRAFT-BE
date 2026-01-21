package com.example.cowmjucraft.global.response;

import com.example.cowmjucraft.global.response.type.ErrorType;
import com.example.cowmjucraft.global.response.type.ResultType;
import com.example.cowmjucraft.global.response.type.SuccessType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "응답 객체")
@JsonPropertyOrder({"resultType", "httpStatusCode", "code", "message", "data"})
public record ApiResponse<T>(

        @Schema(description = "응답 타입", example = "SUCCESS")
        ResultType resultType,

        @Schema(description = "HTTP 상태 코드", example = "200")
        int httpStatusCode,

        @Schema(description = "응답 코드(에러 분기/로그용)", example = "COMMON_400_INVALID_REQUEST")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String code,

        @Schema(description = "응답 메시지", example = "요청에 성공하였습니다.")
        String message,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        T data
) {

    public static <T> ApiResponse<T> success(SuccessType successType, T data) {
        return ApiResponse.<T>builder()
                .resultType(ResultType.SUCCESS)
                .httpStatusCode(successType.getHttpStatusCode())
                .code(null)
                .message(successType.getMessage())
                .data(data)
                .build();
    }

    public static ApiResponse<?> success(SuccessType successType) {
        return success(successType, null);
    }

    public static <T> ApiResponse<T> error(ErrorType errorType, String message, T data) {
        return ApiResponse.<T>builder()
                .resultType(ResultType.FAIL)
                .httpStatusCode(errorType.getHttpStatusCode())
                .code(errorType.getCode())
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<?> error(ErrorType errorType) {
        return error(errorType, errorType.getMessage(), null);
    }

    public static ApiResponse<?> error(ErrorType errorType, String message) {
        return error(errorType, message, null);
    }

    public static <T> ApiResponse<T> error(ErrorType errorType, T data) {
        return error(errorType, errorType.getMessage(), data);
    }
}