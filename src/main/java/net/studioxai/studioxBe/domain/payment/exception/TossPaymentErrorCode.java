package net.studioxai.studioxBe.domain.payment.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TossPaymentErrorCode implements BaseErrorCode {
    // 400 BAD_REQUEST
    INVALID_PAYMENT_REQUEST(HttpStatus.BAD_REQUEST, "PAYMENT_400_1", "결제 요청 값이 올바르지 않습니다."),

    // 502 BAD_GATEWAY
    TOSS_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "PAYMENT_502_1", "토스페이먼츠 요청에 실패했습니다."),
    TOSS_RESPONSE_PARSE_FAILED(HttpStatus.BAD_GATEWAY, "PAYMENT_502_2", "토스페이먼츠 응답 처리에 실패했습니다."),
    TOSS_EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "PAYMENT_502_3", "토스페이먼츠 응답이 비어 있습니다."),

    // 500 INTERNAL_SERVER_ERROR
    PAYMENT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_500_1", "결제 처리 중 서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
