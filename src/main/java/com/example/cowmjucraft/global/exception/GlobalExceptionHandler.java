package com.example.cowmjucraft.global.exception;

import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.ErrorType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
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
    public ResponseEntity<ApiResult<?>> handleBindingErrors(Exception exception) {
        BindingResult bindingResult = (exception instanceof MethodArgumentNotValidException manve)
                ? manve.getBindingResult()
                : ((BindException) exception).getBindingResult();

        String detail = buildFieldErrorDetail(bindingResult);
        String message = buildValidationMessage(detail);
        log.debug("Validation error: {}", detail);

        return json(errorType(ErrorType.VALIDATION_FAILED), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<?>> handleConstraintViolation(ConstraintViolationException exception) {
        List<String> details = new ArrayList<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String path = normalizeConstraintPath(violation.getPropertyPath().toString());
            details.add(path + ": " + violation.getMessage());
        }
        String detail = summarizeDetails(details);
        String message = buildValidationMessage(detail);
        log.debug("Constraint violation: {}", detail);

        return json(errorType(ErrorType.VALIDATION_FAILED), message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResult<?>> handleNotReadable(HttpMessageNotReadableException exception) {
        log.debug("Unreadable request body", exception);
        return json(errorType(ErrorType.INVALID_REQUEST),
                "요청 값 검증에 실패했습니다. (요청 본문 형식이 올바르지 않습니다)");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResult<?>> handleIllegalArgument(IllegalArgumentException exception) {
        log.debug("IllegalArgumentException: {}", exception.getMessage(), exception);
        String message = defaultIfBlank(exception.getMessage(), ErrorType.INVALID_REQUEST.getMessage());
        return json(errorType(ErrorType.INVALID_REQUEST), message);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResult<?>> handleResponseStatus(ResponseStatusException exception) {
        ErrorType errorType = mapStatusToErrorType(exception.getStatusCode());
        String message = defaultIfBlank(exception.getReason(), errorType.getMessage());
        log.debug("ResponseStatusException: status={}, reason={}", exception.getStatusCode(), exception.getReason(), exception);

        return json(errorType(errorType), message);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResult<?>> handleNotAcceptable(HttpMediaTypeNotAcceptableException exception) {
        log.debug("Not acceptable (Accept/produces mismatch)", exception);

        return ResponseEntity.status(406)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResult.error(ErrorType.INVALID_REQUEST, "Accept 헤더가 지원되지 않습니다."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResult<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        log.debug("Method not supported: {}", exception.getMessage(), exception);

        return ResponseEntity.status(405)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResult.error(ErrorType.BAD_REQUEST, "지원하지 않는 HTTP Method 입니다."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResult<?>> handleNoResourceFound(NoResourceFoundException exception) {
        log.debug("No resource found: {}", exception.getMessage());
        return json(errorType(ErrorType.NOT_FOUND), ErrorType.NOT_FOUND.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<?>> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return json(errorType(ErrorType.INTERNAL_ERROR), ErrorType.INTERNAL_ERROR.getMessage());
    }

    private ResponseEntity<ApiResult<?>> json(ErrorType errorType, String message) {
        return ResponseEntity.status(errorType.getHttpStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResult.error(errorType, message));
    }

    private ErrorType errorType(ErrorType t) {
        return t;
    }

    private String buildFieldErrorDetail(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        List<String> details = new ArrayList<>();
        for (FieldError error : fieldErrors) {
            details.add(error.getField() + ": " + error.getDefaultMessage());
        }
        return summarizeDetails(details);
    }

    private String summarizeDetails(List<String> details) {
        if (details.isEmpty()) return "";

        int limit = Math.min(details.size(), MAX_ERROR_DETAILS);
        String joined = details.subList(0, limit).stream().collect(Collectors.joining(", "));
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

    private ErrorType mapStatusToErrorType(HttpStatusCode statusCode) {
        if (statusCode == null) return ErrorType.INTERNAL_ERROR;
        int value = statusCode.value();
        return switch (value) {
            case 400 -> ErrorType.INVALID_REQUEST;
            case 401 -> ErrorType.UNAUTHORIZED;
            case 403 -> ErrorType.FORBIDDEN;
            case 404 -> ErrorType.NOT_FOUND;
            case 409 -> ErrorType.CONFLICT;
            default -> ErrorType.INTERNAL_ERROR;
        };
    }
}
