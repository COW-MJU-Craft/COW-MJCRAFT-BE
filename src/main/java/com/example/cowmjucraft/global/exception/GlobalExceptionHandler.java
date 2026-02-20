package com.example.cowmjucraft.global.exception;

import com.example.cowmjucraft.global.response.ApiResult;
import com.example.cowmjucraft.global.response.type.CommonErrorType;
import com.example.cowmjucraft.global.response.type.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

        return json(CommonErrorType.VALIDATION_FAILED, message);
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

        return json(CommonErrorType.VALIDATION_FAILED, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResult<?>> handleNotReadable(HttpMessageNotReadableException exception) {
        log.debug("Unreadable request body", exception);
        return json(CommonErrorType.INVALID_REQUEST,
                "요청 값 검증에 실패했습니다. (요청 본문 형식이 올바르지 않습니다)");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResult<?>> handleIllegalArgument(IllegalArgumentException exception) {
        log.debug("IllegalArgumentException: {}", exception.getMessage(), exception);
        String message = defaultIfBlank(exception.getMessage(), CommonErrorType.INVALID_REQUEST.getMessage());
        return json(CommonErrorType.INVALID_REQUEST, message);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResult<?>> handleResponseStatus(ResponseStatusException exception) {
        CommonErrorType errorType = mapStatusToErrorType(exception.getStatusCode());
        String message = defaultIfBlank(exception.getReason(), errorType.getMessage());
        log.debug("ResponseStatusException: status={}, reason={}", exception.getStatusCode(), exception.getReason(), exception);

        return json(errorType, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResult<?>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        log.debug("DataIntegrityViolationException: {}", exception.getMessage(), exception);
        if (isLookupIdUniqueViolation(exception)) {
            return json(CommonErrorType.CONFLICT, "이미 사용 중인 조회 아이디입니다.");
        }
        return json(CommonErrorType.CONFLICT, CommonErrorType.CONFLICT.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResult<?>> handleNotAcceptable(HttpMediaTypeNotAcceptableException exception) {
        log.debug("Not acceptable (Accept/produces mismatch)", exception);

        return ResponseEntity.status(406)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResult.error(CommonErrorType.INVALID_REQUEST, "Accept 헤더가 지원되지 않습니다."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResult<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        log.debug("Method not supported: {}", exception.getMessage(), exception);

        return ResponseEntity.status(405)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResult.error(CommonErrorType.BAD_REQUEST, "지원하지 않는 HTTP Method 입니다."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResult<?>> handleNoResourceFound(NoResourceFoundException exception) {
        log.debug("No resource found: {}", exception.getMessage());
        return json(CommonErrorType.NOT_FOUND, CommonErrorType.NOT_FOUND.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResult<?>> handleDomainException(DomainException exception) {
        return json(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<?>> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return json(CommonErrorType.INTERNAL_ERROR, CommonErrorType.INTERNAL_ERROR.getMessage());
    }


    private ResponseEntity<ApiResult<?>> json(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResult.error(errorCode, message));
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

    private boolean isLookupIdUniqueViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                boolean hasUniqueSignal = lower.contains("duplicate")
                        || lower.contains("duplicate entry")
                        || lower.contains("unique constraint")
                        || lower.contains("violates unique constraint")
                        || lower.contains("constraint")
                        || lower.contains("uk_");
                boolean hasLookupSignal = lower.contains("lookup_id")
                        || lower.contains("lookupid")
                        || lower.contains("lookup");
                boolean hasOrderAuthLookup = lower.contains("order_auth") && hasLookupSignal;
                if ((hasUniqueSignal && hasLookupSignal) || hasOrderAuthLookup) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private CommonErrorType mapStatusToErrorType(HttpStatusCode statusCode) {
        if (statusCode == null) return CommonErrorType.INTERNAL_ERROR;
        int value = statusCode.value();
        return switch (value) {
            case 400 -> CommonErrorType.INVALID_REQUEST;
            case 401 -> CommonErrorType.UNAUTHORIZED;
            case 403 -> CommonErrorType.FORBIDDEN;
            case 404 -> CommonErrorType.NOT_FOUND;
            case 410 -> CommonErrorType.GONE;
            case 409 -> CommonErrorType.CONFLICT;
            default -> CommonErrorType.INTERNAL_ERROR;
        };
    }
}
