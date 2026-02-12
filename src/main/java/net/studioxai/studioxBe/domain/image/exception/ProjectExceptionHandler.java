package net.studioxai.studioxBe.domain.image.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class ProjectExceptionHandler extends BaseErrorException {
    public ProjectExceptionHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
