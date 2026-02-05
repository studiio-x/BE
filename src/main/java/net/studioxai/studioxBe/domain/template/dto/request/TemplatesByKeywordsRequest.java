package net.studioxai.studioxBe.domain.template.dto.request;

import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;

import java.util.List;

public record TemplatesByKeywordsRequest(
        List<TemplateKeywordType> keywords,
        int limitPerKeyword
) {}

