package net.studioxai.studioxBe.domain.template.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.studioxai.studioxBe.global.dto.ErrorReason;
import net.studioxai.studioxBe.global.error.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TemplateManagerErrorCode implements BaseErrorCode {

    TEMPLATE_NOT_FOUND_BY_CATEGORY(HttpStatus.NOT_FOUND, "TEMPLATEMANAGER_404_1", "해당 카테고리의 템플릿이 존재하지 않습니다."),
    TEMPLATE_NOT_FOUND_BY_KEYWORD(HttpStatus.NOT_FOUND, "TEMPLATEMANAGER_404_2", "해당 키워드의 템플릿이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {return ErrorReason.of(status.value(), code, reason);}
}
