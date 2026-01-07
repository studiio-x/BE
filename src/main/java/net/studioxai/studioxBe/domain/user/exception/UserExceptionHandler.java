package net.studioxai.studioxBe.domain.user.exception;

import net.studioxai.studioxBe.global.error.BaseErrorException;

public class UserExceptionHandler extends BaseErrorException {
    public UserExceptionHandler(UserErrorCode errorCode) {
        super(errorCode);
    }
}
