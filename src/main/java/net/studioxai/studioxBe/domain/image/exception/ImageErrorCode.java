package net.studioxai.studioxBe.domain.image.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements BaseErrorCode {
    FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGE_404_1", "해당 폴더가 존재하지 않습니다."),
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGE_404_2", "해당 템플릿이가 존재하지 않습니다."),

    IMAGE_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "IMAGE_502_1", "이미지 생성 중 오류가 발생했습니다."),

    ;
    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }

}
