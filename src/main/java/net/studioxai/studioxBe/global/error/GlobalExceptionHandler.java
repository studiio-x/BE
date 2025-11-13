package net.studioxai.studioxBe.global.error;

import net.studioxai.studioxBe.global.dto.ErrorReason;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {

        final HttpStatus status = HttpStatus.valueOf(statusCode.value());
        GlobalErrorCode globalErrorCode = mapToGlobalErrorCode(status);
        final ErrorReason errorReason = globalErrorCode.getErrorReason();
        final ErrorResponse errorResponse = ErrorResponse.from(errorReason);

        log.error("HandleInternalException - status: {}, globalCode: {}, uri: {}, message: {}",
                status.value(),
                globalErrorCode.name(),
                request.getDescription(false),
                ex.getMessage(),
                ex);

        return super.handleExceptionInternal(ex, errorResponse, headers, status, request);
    }

    // 유효성 검사 실패
    @Nullable
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<FieldErrorDetail> errorDetails =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(err -> new FieldErrorDetail(
                                err.getField(),
                                err.getDefaultMessage() == null ? "" : err.getDefaultMessage()
                        ))
                        .toList();

        GlobalErrorCode globalErrorCode = GlobalErrorCode.INVALID_INPUT;
        final ErrorReason errorReason = globalErrorCode.getErrorReason();
        final ErrorResponse errorResponse = ErrorResponse.from(errorReason, errorDetails);

        log.warn("MethodArgumentNotValid - status: {}, uri: {}, errors: {}",
                globalErrorCode.getStatus().value(),
                request.getDescription(false),
                errorDetails);

        return ResponseEntity.status(globalErrorCode.getStatus())
                .body(errorResponse);
    }

    // request body 읽기 실패
    @Nullable
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        final GlobalErrorCode globalErrorCode = GlobalErrorCode.WRONG_FORMAT_VALUE;
        final ErrorReason errorReason = globalErrorCode.getErrorReason();
        final ErrorResponse errorResponse = ErrorResponse.from(errorReason);

        log.warn("HttpMessageNotReadable - uri: {}, message: {}",
                request.getDescription(false),
                ex.getMessage(),
                ex);

        return ResponseEntity.status(globalErrorCode.getStatus()).body(errorResponse);
    }

    // 비즈니스 로직 에러 처리
    @ExceptionHandler(BaseErrorException.class)
    public ResponseEntity<ErrorResponse> handleBaseErrorException(
            BaseErrorException e, HttpServletRequest request) {

        log.warn("BaseErrorException - uri: {}, method: {}, code: {}, message: {}",
                request.getRequestURI(),
                request.getMethod(),
                e.getErrorCode().toString(),
                e.getMessage(),
                e);

        final ErrorReason errorReason = e.getErrorCode().getErrorReason();
        final ErrorResponse errorResponse = ErrorResponse.from(errorReason);
        return ResponseEntity.status(HttpStatus.valueOf(errorReason.getStatus()))
                .body(errorResponse);
    }

    // 전역 오류 관리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(
            Exception e, HttpServletRequest request) {

        final GlobalErrorCode globalErrorCode = GlobalErrorCode.UNCAUGHT_EXCEPTION;
        final ErrorReason errorReason = globalErrorCode.getErrorReason();
        final ErrorResponse errorResponse = ErrorResponse.from(errorReason);

        log.error("Uncaught exception - uri: {}, method: {}, message: {}",
                request.getRequestURI(),
                request.getMethod(),
                e.getMessage(),
                e);

        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private GlobalErrorCode mapToGlobalErrorCode(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> GlobalErrorCode.GLOBAL_NOT_FOUND;
            case METHOD_NOT_ALLOWED -> GlobalErrorCode.GLOBAL_METHOD_NOT_ALLOWED;
            case UNSUPPORTED_MEDIA_TYPE -> GlobalErrorCode.GLOBAL_UNSUPPORTED_MEDIA_TYPE;
            case BAD_REQUEST -> GlobalErrorCode.INVALID_INPUT; // 기본
            default -> GlobalErrorCode.UNCAUGHT_EXCEPTION;
        };
    }
}