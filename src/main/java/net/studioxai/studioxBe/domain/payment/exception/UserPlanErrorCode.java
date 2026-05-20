package net.studioxai.studioxBe.domain.payment.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.payment.entity.UserPlan;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserPlanErrorCode implements BaseErrorCode {
    USER_PLAN_NOT_FOUNT(HttpStatus.NOT_FOUND, "USER_PLAN_404_1", "user plan이 아직 생성되지 않은 유저입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
