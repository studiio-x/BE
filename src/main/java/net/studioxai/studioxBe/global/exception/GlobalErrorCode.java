package net.studioxai.studioxBe.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {
    // 전역 오류
    WRONG_FORMAT_VALUE(BAD_REQUEST, "GLOBAL_400_1", "잘못된 형식의 값을 입력했습니다."),
    UNCAUGHT_EXCEPTION(INTERNAL_SERVER_ERROR, "GLOBAL_500_1", "서버 오류"),

    // 토큰 관련
    NO_TOKEN(UNAUTHORIZED, "AUTH_401_1", "토큰이 존재하지 않습니다"),
    INVALID_TOKEN(UNAUTHORIZED, "AUTH_401_2", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(UNAUTHORIZED, "AUTH_401_3", "만료된 토큰입니다"),
    ACCESS_DENIED(FORBIDDEN, "AUTH_403_1", "접근 권한이 없습니다")

    ;

    private HttpStatus status;
    private String code;
    private String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
