package net.studioxai.studioxBe.domain.template.dto.response;

import net.studioxai.studioxBe.global.dto.PageInfo;

import java.util.List;

public record TemplateCategoryGet(
        List<TemplateByCategoryResponse> templates,
        PageInfo pageInfo
) {}
