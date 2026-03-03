package com.example.cowmjucraft.global.exception;

import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.CommonErrorType;
import com.example.cowmjucraft.global.response.type.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final int MAX_ERROR_DETAILS = 5;

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResult<?>> handleBindingErrors(BindException exception) {
        String detail = buildFieldErrorDetail(exception.getBindingResult().getFieldErrors());
        String message = buildValidationMessage(detail);
        log.debug("유효성 검사 오류: {}", detail);

        return json(CommonErrorType.VALIDATION_FAILED, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<?>> handleConstraintViolation(ConstraintViolationException exception) {
        List<String> details = exception.getConstraintViolations().stream()
                .map(v -> normalizeConstraintPath(v.getPropertyPath().toString()) + ": " + v.getMessage())
                .toList();
        String detail = summarizeDetails(details);
        String message = buildValidationMessage(detail);
        log.debug("제약 조건 위반: {}", detail);

        return json(CommonErrorType.VALIDATION_FAILED, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResult<?>> handleNotReadable(HttpMessageNotReadableException exception) {
        log.warn("요청 본문을 읽을 수 없습니다", exception);
        return json(CommonErrorType.INVALID_REQUEST,
                "요청 값 검증에 실패했습니다. (요청 본문 형식이 올바르지 않습니다)");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResult<?>> handleIllegalArgument(IllegalArgumentException exception) {
        log.warn("잘못된 인자값: {}", exception.getMessage(), exception);
        String message = defaultIfBlank(exception.getMessage(), CommonErrorType.INVALID_REQUEST.getMessage());
        return json(CommonErrorType.INVALID_REQUEST, message);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResult<?>> handleResponseStatus(ResponseStatusException exception) {
        CommonErrorType errorType = mapStatusToErrorType(exception.getStatusCode());
        String message = defaultIfBlank(exception.getReason(), errorType.getMessage());
        log.warn("HTTP 상태 예외: 상태={}, 사유={}", exception.getStatusCode(), exception.getReason(), exception);

        return json(errorType, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResult<?>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        log.warn("데이터 무결성 위반: {}", exception.getMessage(), exception);
        return json(CommonErrorType.CONFLICT, CommonErrorType.CONFLICT.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResult<?>> handleNotAcceptable(HttpMediaTypeNotAcceptableException exception) {
        log.warn("지원하지 않는 미디어 타입 요청", exception);
        return json(CommonErrorType.NOT_ACCEPTABLE, CommonErrorType.NOT_ACCEPTABLE.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResult<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        log.warn("지원하지 않는 HTTP 메서드: {}", exception.getMessage(), exception);
        return json(CommonErrorType.METHOD_NOT_ALLOWED, CommonErrorType.METHOD_NOT_ALLOWED.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResult<?>> handleNoResourceFound(NoResourceFoundException exception) {
        log.debug("리소스를 찾을 수 없음: {}", exception.getMessage());
        return json(CommonErrorType.NOT_FOUND, CommonErrorType.NOT_FOUND.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResult<?>> handleDomainException(DomainException exception) {
        log.info("도메인 예외: [{}] {}", exception.getErrorCode(), exception.getMessage());
        return json(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<?>> handleException(Exception exception) {
        log.error("처리되지 않은 예외", exception);
        return json(CommonErrorType.INTERNAL_ERROR, CommonErrorType.INTERNAL_ERROR.getMessage());
    }


    private ResponseEntity<ApiResult<?>> json(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResult.error(errorCode, message));
    }

    private String buildFieldErrorDetail(List<FieldError> fieldErrors) {
        List<String> details = fieldErrors.stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return summarizeDetails(details);
    }

    private String summarizeDetails(List<String> details) {
        if (details.isEmpty()) return "";

        int limit = Math.min(details.size(), MAX_ERROR_DETAILS);
        String joined = String.join(", ", details.subList(0, limit));
        int remaining = details.size() - limit;

        if (remaining > 0) joined = joined + ", 외 " + remaining + "건";
        return joined;
    }

    private String buildValidationMessage(String detail) {
        if (detail == null || detail.isBlank()) return "요청 값 검증에 실패했습니다.";
        return "요청 값 검증에 실패했습니다. (" + detail + ")";
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String normalizeConstraintPath(String path) {
        int requestIndex = path.indexOf("request.");
        if (requestIndex >= 0) return path.substring(requestIndex + "request.".length());
        return path;
    }

    private CommonErrorType mapStatusToErrorType(HttpStatusCode statusCode) {
        if (statusCode == null) return CommonErrorType.INTERNAL_ERROR;
        int value = statusCode.value();
        return switch (value) {
            case 400 -> CommonErrorType.INVALID_REQUEST;
            case 401 -> CommonErrorType.UNAUTHORIZED;
            case 403 -> CommonErrorType.FORBIDDEN;
            case 404 -> CommonErrorType.NOT_FOUND;
            case 409 -> CommonErrorType.CONFLICT;
            case 410 -> CommonErrorType.GONE;
            case 422 -> CommonErrorType.VALIDATION_FAILED;
            case 429 -> CommonErrorType.TOO_MANY_REQUESTS;
            default -> CommonErrorType.INTERNAL_ERROR;
        };
    }
}
