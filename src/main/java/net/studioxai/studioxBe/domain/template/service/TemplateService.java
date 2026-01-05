package net.studioxai.studioxBe.domain.template.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateResponse;
import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;
import net.studioxai.studioxBe.domain.template.repository.TemplateRepository;
import net.studioxai.studioxBe.global.entity.enums.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final TemplateRepository templateRepository;

    public List<TemplateResponse> getTemplatesByCategory(Category category) {
        return templateRepository.findByCategory(category)
                .stream()
                .map(TemplateResponse::from)
                .toList();
    }


    public List<TemplateResponse> getTemplatesByKeyword(TemplateKeywordType keyword) {
        return templateRepository.findByKeyword(keyword)
                .stream()
                .map(TemplateResponse::from)
                .toList();
    }

}
