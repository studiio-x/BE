package net.studioxai.studioxBe.domain.template.dto.response;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record TemplateByCategoryResponse(
        Long templateId,
        @ImageUrl String imageUrl
) {
}
