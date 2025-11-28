package net.studioxai.studioxBe.domain.auth.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class AuthExceptionHandler extends BaseErrorException
{

    public AuthExceptionHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
