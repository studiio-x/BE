package net.studioxai.studioxBe.domain.payment.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExchangeRateErrorCode implements BaseErrorCode {
    // 400 Bad Request
    INVALID_API_CALL(HttpStatus.BAD_REQUEST, "EXCHANTE_RATE_400_1", "환율 API 호출에 실패했습니다."),


    // 404 Not Found
    NOT_FOUND_KRW(HttpStatus.NOT_FOUND, "EXCHANGE_RATE_404_1", "환율 응답에 KRW 값이 없습니다."),
    NOT_FOUND_EXCHANGE_RATE(HttpStatus.NOT_FOUND, "EXCHANGE_RATE_404_2", "저장된 환율 정보가 없습니다."),
    NOT_FOUND_RESPONSE(HttpStatus.NOT_FOUND, "EXCHANGE_RATE_404_3", "환율 API 응답이 비어있습니다."),
    NOT_FOUND_CONVERSION_RATE(HttpStatus.NOT_FOUND, "EXCHANGE_RATE_404_4", "환율 API 응답에 conversion-rates가 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}


