package com.example.cowmjucraft.global.response;

import com.example.cowmjucraft.global.response.type.ErrorCode;
import com.example.cowmjucraft.global.response.type.ResultType;
import com.example.cowmjucraft.global.response.type.SuccessType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "응답 객체")
@JsonPropertyOrder({"resultType", "httpStatusCode", "message", "data"})
public record ApiResult<T>(

        @Schema(description = "응답 타입", example = "SUCCESS")
        ResultType resultType,

        @Schema(description = "HTTP 상태 코드", example = "200")
        int httpStatusCode,

        @Schema(description = "응답 메시지", example = "요청에 성공하였습니다.")
        String message,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        T data
) {

    public static <T> ApiResult<T> success(SuccessType successType, T data) {
        return ApiResult.<T>builder()
                .resultType(ResultType.SUCCESS)
                .httpStatusCode(successType.getHttpStatusCode())
                .message(successType.getMessage())
                .data(data)
                .build();
    }

    public static ApiResult<?> success(SuccessType successType) {
        return success(successType, null);
    }

    public static <T> ApiResult<T> error(ErrorCode errorCode, String message, T data) {
        return ApiResult.<T>builder()
                .resultType(ResultType.FAIL)
                .httpStatusCode(errorCode.getHttpStatusCode())
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResult<?> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getMessage(), null);
    }

    public static ApiResult<?> error(ErrorCode errorCode, String message) {
        return error(errorCode, message, null);
    }

    public static <T> ApiResult<T> error(ErrorCode errorCode, T data) {
        return error(errorCode, errorCode.getMessage(), data);
    }
}
