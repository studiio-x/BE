package net.studioxai.studioxBe.domain.payment.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class UserPlanExceptionHandler extends BaseErrorException {
    public UserPlanExceptionHandler(BaseErrorCode baseErrorCode) {
        super(baseErrorCode);
    }
}
