package net.studioxai.studioxBe.domain.folder.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class FolderExceptionHandler extends BaseErrorException {
    public FolderExceptionHandler(BaseErrorCode baseErrorCode) {
        super(baseErrorCode);
    }
}
