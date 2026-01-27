package net.studioxai.studioxBe.domain.template.dto.response;

import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;

import java.util.List;

public record KeywordTemplatesResponse(
        TemplateKeywordType keyword,
        String keywordTitle,
        List<TemplateByKeywordResponse> templates
) {
    public KeywordTemplatesResponse(
            TemplateKeywordType keyword,
            List<TemplateByKeywordResponse> templates
    ) {
        this(keyword, keyword.getTitle(), templates);
    }
}


