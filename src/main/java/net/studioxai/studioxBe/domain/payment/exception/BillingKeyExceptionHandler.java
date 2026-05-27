package net.studioxai.studioxBe.domain.payment.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class BillingKeyExceptionHandler extends BaseErrorException {
    public BillingKeyExceptionHandler(BaseErrorCode baseErrorCode) { super(baseErrorCode); }
}
