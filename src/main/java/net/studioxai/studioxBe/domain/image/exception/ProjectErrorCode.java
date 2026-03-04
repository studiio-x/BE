package net.studioxai.studioxBe.domain.image.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProjectErrorCode implements BaseErrorCode {
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_404_1", "해당 프로젝트를 찾을 수 없습니다."),

    ;
    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }

}
