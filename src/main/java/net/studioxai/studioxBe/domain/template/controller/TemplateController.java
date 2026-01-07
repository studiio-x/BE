package net.studioxai.studioxBe.domain.template.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.template.dto.TemplateCategoryGet;
import net.studioxai.studioxBe.domain.template.dto.TemplateKeywordGet;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateByCategoryResponse;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateByKeywordResponse;
import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;
import net.studioxai.studioxBe.domain.template.service.TemplateService;
import net.studioxai.studioxBe.global.entity.enums.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping("/v1/templates/category")
    public TemplateCategoryGet getTemplatesByCategory(@RequestParam Category category, @RequestParam int pageNum, @RequestParam int limit) {
        return templateService.getTemplatesByCategory(category, pageNum, limit);
    }

    @GetMapping("/v1/templates/keyword")
    public TemplateKeywordGet getTemplatesByKeyword(@RequestParam TemplateKeywordType keyword, @RequestParam int pageNum, @RequestParam int limit) {
        return templateService.getTemplatesByKeyword(keyword, pageNum, limit);
    }
}
