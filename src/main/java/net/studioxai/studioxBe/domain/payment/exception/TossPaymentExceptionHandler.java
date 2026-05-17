package net.studioxai.studioxBe.domain.payment.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class TossPaymentExceptionHandler extends BaseErrorException {
    public TossPaymentExceptionHandler(BaseErrorCode baseErrorCode) {
        super(baseErrorCode);
    }
}
