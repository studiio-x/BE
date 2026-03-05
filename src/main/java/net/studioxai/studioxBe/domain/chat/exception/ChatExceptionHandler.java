package net.studioxai.studioxBe.domain.chat.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class ChatExceptionHandler extends BaseErrorException {
    public ChatExceptionHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
