package net.studioxai.studioxBe.domain.template.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateByKeywordResponse;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateCategoryGet;
import net.studioxai.studioxBe.domain.template.dto.response.KeywordTemplatesResponse;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateKeywordResponse;
import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;
import net.studioxai.studioxBe.domain.template.service.TemplateService;
import net.studioxai.studioxBe.global.entity.enums.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/v1/templates/template-keywords")
    public List<TemplateKeywordResponse> getTemplateKeywords() {
        return templateService.getAllTemplateKeywords();
    }

    @GetMapping("/v1/templates/keyword")
    public List<KeywordTemplatesResponse> getTemplatesByKeywords(@RequestParam List<TemplateKeywordType> keywords, @RequestParam int limitPerKeyword) {
        return templateService.getTemplatesByKeywords(keywords, limitPerKeyword);
    }

    @GetMapping("/v1/templates/search")
    public List<TemplateByKeywordResponse> searchTemplates(@RequestParam String keyword) {
        return templateService.searchTemplatesByKeyword(keyword);
    }


}
