package net.studioxai.studioxBe.domain.project.exception;

import net.studioxai.studioxBe.global.error.BaseErrorCode;
import net.studioxai.studioxBe.global.error.BaseErrorException;

public class ProjectMangerExceptionHandler extends BaseErrorException {
  public ProjectMangerExceptionHandler(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
