package net.studioxai.studioxBe.domain.template.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class TemplateManagerExceptionHandler extends BaseErrorException {
    public TemplateManagerExceptionHandler(BaseErrorCode errorCode) {super(errorCode);}
}
