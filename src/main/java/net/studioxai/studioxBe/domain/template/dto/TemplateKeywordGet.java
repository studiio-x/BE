package net.studioxai.studioxBe.domain.template.dto;

import net.studioxai.studioxBe.domain.template.dto.response.TemplateByKeywordResponse;
import net.studioxai.studioxBe.global.dto.PageInfo;

import java.util.List;

public record TemplateKeywordGet(
        List<TemplateByKeywordResponse> templates,
        PageInfo pageInfo
) {}
