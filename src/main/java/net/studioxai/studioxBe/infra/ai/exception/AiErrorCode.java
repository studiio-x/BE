package net.studioxai.studioxBe.infra.ai.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AiErrorCode implements BaseErrorCode {
    AI_CALL_FAILED(HttpStatus.BAD_GATEWAY, "AI_502_1", "AI 서버 호출에 실패했습니다."),
    AI_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "AI_502_2", "AI 서버로부터 올바르지 않은 응답을 받았습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }

}
