package net.studioxai.studioxBe.domain.folder.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FolderErrorCode implements BaseErrorCode {
    // 400 BAD_REQUEST
    PARENT_REQUIRED(HttpStatus.BAD_REQUEST, "FOLDER_400_1", "상위 폴더 혹은 프로젝트를 지정해야 합니다."),;


    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
