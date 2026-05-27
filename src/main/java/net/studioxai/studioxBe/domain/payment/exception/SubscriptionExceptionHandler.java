package net.studioxai.studioxBe.domain.payment.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class SubscriptionExceptionHandler extends BaseErrorException {
    public SubscriptionExceptionHandler(BaseErrorCode baseErrorCode) {
        super(baseErrorCode);
    }
}
