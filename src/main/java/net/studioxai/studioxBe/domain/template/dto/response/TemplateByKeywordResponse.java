package net.studioxai.studioxBe.domain.template.dto.response;

import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;
import net.studioxai.studioxBe.global.entity.enums.Category;

public record TemplateByKeywordResponse(
        Long templateId,
        TemplateKeywordType keyword,
        String imageUrl,
        Category category
) {
}
