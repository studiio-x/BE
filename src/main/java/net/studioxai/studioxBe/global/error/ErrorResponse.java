package net.studioxai.studioxBe.global.error;

import net.studioxai.studioxBe.global.dto.ErrorReason;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ErrorResponse {
    private final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    private final boolean success = false;
    private String code;
    private String status;
    private String reason;
    private List<FieldErrorDetail> errors;

    @Builder
    private ErrorResponse(String code, String status, String reason, List<FieldErrorDetail> errors) {
        this.code = code;
        this.status = status;
        this.reason = reason;
        this.errors = errors;
    }

    public static ErrorResponse from(ErrorReason errorReason) {
        return ErrorResponse.builder()
                .code(errorReason.getCode())
                .status(errorReason.getStatus().toString())
                .reason(errorReason.getReason())
                .errors(null)
                .build();
    }
    public static ErrorResponse from(ErrorReason errorReason, List<FieldErrorDetail> errors) {
        return ErrorResponse.builder()
                .code(errorReason.getCode())
                .status(errorReason.getStatus().toString())
                .reason(errorReason.getReason())
                .errors(errors)
                .build();
    }
}
