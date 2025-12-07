package net.studioxai.studioxBe.domain.folder.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class FolderManagerExceptionHandler extends BaseErrorException {
    public FolderManagerExceptionHandler(BaseErrorCode errorCode) {
      super(errorCode);
    }
}
