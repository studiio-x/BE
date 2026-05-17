package net.studioxai.studioxBe.domain.payment.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BillingKeyErrorCode implements BaseErrorCode {
    // 400 Bad Request
    INVALID_CUSTOM_KEY(HttpStatus.BAD_REQUEST, "BILLING_KEY_400_1", "커스텀 키가 일치하지 않습니다.");


    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}

