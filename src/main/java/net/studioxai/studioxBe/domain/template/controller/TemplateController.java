package net.studioxai.studioxBe.domain.template.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateResponse;
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
    public List<TemplateResponse> getTemplatesByCategory(@RequestParam Category category) {

        return templateService.getTemplatesByCategory(category);
    }

    @GetMapping("/v1/templates/keyword")
    public List<TemplateResponse> getTemplatesByKeyword(@RequestParam TemplateKeywordType keyword) {

        return templateService.getTemplatesByKeyword(keyword);
    }
}
