package net.studioxai.studioxBe.domain.template.dto.response;

import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.global.entity.enums.Category;

public record TemplateResponse(
        Long id,
        Category category,
        String imageUrl
) {
    public static TemplateResponse from(Template template) {
        return new TemplateResponse(
                template.getId(),
                template.getCategory(),
                template.getImageUrl()
        );
    }
}
