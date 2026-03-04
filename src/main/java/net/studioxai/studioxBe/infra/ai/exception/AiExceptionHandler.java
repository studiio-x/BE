package net.studioxai.studioxBe.infra.ai.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class AiExceptionHandler extends BaseErrorException {

    public AiExceptionHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}