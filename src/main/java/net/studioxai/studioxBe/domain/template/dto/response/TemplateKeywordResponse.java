package net.studioxai.studioxBe.domain.template.dto.response;

import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;

public record TemplateKeywordResponse(
        TemplateKeywordType keyword,
        String title
) {
    public TemplateKeywordResponse(TemplateKeywordType keyword) {
        this(keyword, keyword.getTitle());
    }
}
