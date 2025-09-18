package net.studioxai.studioxBe.global.exception;

import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorResponse {
    private final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    private final boolean success = false;
    private String code;
    private String status;
    private String reason;

    @Builder
    private ErrorResponse(String code, String status, String reason) {
        this.code = code;
        this.status = status;
        this.reason = reason;
    }

    public static ErrorResponse from(ErrorReason errorReason) {
        return ErrorResponse.builder()
                .code(errorReason.getCode())
                .status(errorReason.getStatus().toString())
                .reason(errorReason.getReason())
                .build();
    }
}
