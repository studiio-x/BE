package net.studioxai.studioxBe.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
    INVALID_EMAIL_TOKEN(HttpStatus.BAD_REQUEST, "USER_400_1", "유효하지 않은 토큰입니다."),
    INVALID_LOGIN_PATH(HttpStatus.BAD_REQUEST, "USER_400_2", "잘못된 로그인 방식입니다."),

    WRONG_ID_OR_PASSWORD(HttpStatus.UNAUTHORIZED, "USER_401_1", "아이디 또는 비밀번호가 일치하지 않습니다."),

    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "USER_403_1", "이메일 인증이 필요합니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_1", "해당 사용자를 찾을 수 없습니다."),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_2", "검증을 요청한 내역이 없습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_3", "이메일이 검증되지 않았습니다."),

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_409_1", "이미 존재하는 이메일입니다."),
    USER_ALREADY_REGISTERS(HttpStatus.CONFLICT, "USER_409_2", "이미 가입된 이메일 계정입니다."),

    FAIL_SENDING_MAIL(HttpStatus.SERVICE_UNAVAILABLE, "USER_503_1", "메일 전송에 실패했습니다.")

    ;

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
