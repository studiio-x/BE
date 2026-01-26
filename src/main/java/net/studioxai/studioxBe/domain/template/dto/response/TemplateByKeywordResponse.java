package net.studioxai.studioxBe.domain.template.dto.response;

import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;
import net.studioxai.studioxBe.global.entity.enums.Category;

import java.util.List;

public record TemplateByKeywordResponse(
        TemplateKeywordType keyword,
        String keywordTitle,
        List<TemplateByKeywordResponse> templates
) {
    public TemplateByKeywordResponse(
            TemplateKeywordType keyword,
            List<TemplateByKeywordResponse> templates
    ) {
        this(keyword, keyword.getTitle(), templates);
    }
}
