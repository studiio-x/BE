package net.studioxai.studioxBe.domain.payment.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class ExchangeRateExceptionHandler extends BaseErrorException {
    public ExchangeRateExceptionHandler(BaseErrorCode baseErrorCode) {
        super(baseErrorCode);
    }
}
