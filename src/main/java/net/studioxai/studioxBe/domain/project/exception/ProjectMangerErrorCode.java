package net.studioxai.studioxBe.domain.project.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProjectMangerErrorCode implements BaseErrorCode {
    USER_NO_PROJECT_AUTHORITY(HttpStatus.FORBIDDEN, "PROJECTMANAGER_403_1", "해당 프로젝트에 대한 권한이 없습니다."),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECTMANAGER_404_1", "해당하는 프로젝트가 존재하지 않습니다."),;


    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
