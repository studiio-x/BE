package net.studioxai.studioxBe.domain.folder.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FolderManagerErrorCode implements BaseErrorCode {
    USER_NO_FOLDER_AUTHORITY(HttpStatus.FORBIDDEN, "FOLDERMANAGER_403_1", "해당 폴더에 대한 권한이 없습니다."),
    FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, "FOLDERMANAGER_404_1", "해당하는 폴더가 존재하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
