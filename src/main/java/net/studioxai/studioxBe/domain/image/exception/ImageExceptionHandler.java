package net.studioxai.studioxBe.domain.image.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class ImageExceptionHandler extends BaseErrorException {
    public ImageExceptionHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
