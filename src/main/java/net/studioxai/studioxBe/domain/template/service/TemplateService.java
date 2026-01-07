package net.studioxai.studioxBe.domain.template.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateByCategoryResponse;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateByKeywordResponse;
import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;
import net.studioxai.studioxBe.domain.template.repository.TemplateKeywordRepository;
import net.studioxai.studioxBe.domain.template.repository.TemplateRepository;
import net.studioxai.studioxBe.global.entity.enums.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateKeywordRepository templateKeywordRepository;

    public List<TemplateByCategoryResponse> getTemplatesByCategory(Category category) {
        return templateRepository.findByCategoryOrderByCreatedAtDesc(category);
    }


    public List<TemplateByKeywordResponse> getTemplatesByKeyword(TemplateKeywordType keyword) {
        return templateKeywordRepository
                .findByKeywordOrderByTemplateCreatedAtDesc(keyword);
    }

}
