package net.studioxai.studioxBe.domain.auth.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class UserExceptionHandler extends BaseErrorException
{

    public UserExceptionHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
