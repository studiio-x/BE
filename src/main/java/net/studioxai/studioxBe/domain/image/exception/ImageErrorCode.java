package net.studioxai.studioxBe.domain.image.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements BaseErrorCode {
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGE_404_1", "해당 템플릿이 존재하지 않습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGE_404_2", "이미지를 찾을 수 없습니다."),

    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_500_1", "S3 업로드에 실패했습니다."),
    S3_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_500_2", "S3 다운로드에 실패했습니다."),
    TEMPLATE_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_500_3", "템플릿을 가져오지 못했습니다."),

    AI_IMAGE_DOWNLOAD_FAILED(HttpStatus.BAD_GATEWAY, "IMAGE_502_1", "이미지 다운로드에 실패했습니다."),
    GEMINI_RESPONSE_INVALID(HttpStatus.BAD_REQUEST, "IMAGE_502_2", "gemini로부터 유효하지 않거나 원하지 않는 응답이 왔습니다."),


    ;
    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }

}
