package net.studioxai.studioxBe.infra.redis.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class TokenExceptionHandler extends BaseErrorException {
    public TokenExceptionHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
