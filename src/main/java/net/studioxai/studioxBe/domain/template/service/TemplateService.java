package net.studioxai.studioxBe.domain.template.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.template.dto.TemplateCategoryGet;
import net.studioxai.studioxBe.domain.template.dto.TemplateKeywordGet;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateByCategoryResponse;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateByKeywordResponse;
import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;
import net.studioxai.studioxBe.domain.template.exception.TemplateManagerErrorCode;
import net.studioxai.studioxBe.domain.template.exception.TemplateManagerExceptionHandler;
import net.studioxai.studioxBe.domain.template.repository.TemplateKeywordRepository;
import net.studioxai.studioxBe.domain.template.repository.TemplateRepository;
import net.studioxai.studioxBe.global.dto.PageInfo;
import net.studioxai.studioxBe.global.entity.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;


import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateKeywordRepository templateKeywordRepository;

    public TemplateCategoryGet getTemplatesByCategory(Category category, int pageNum, int limit) {

        Pageable pageable = PageRequest.of(pageNum, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Template> templates = templateRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);

        if (templates.isEmpty()) {
            throw new TemplateManagerExceptionHandler(TemplateManagerErrorCode.TEMPLATE_NOT_FOUND_BY_CATEGORY);
        }

        List<TemplateByCategoryResponse> contents =
                templates.getContent().stream()
                        .map(t -> new TemplateByCategoryResponse(
                                t.getId(),
                                t.getImageUrl()
                        ))
                        .toList();

        PageInfo pageInfo = PageInfo.of(
                pageNum,
                limit,
                templates.getTotalPages(),
                templates.getTotalElements()
        );

        return new TemplateCategoryGet(contents, pageInfo);
    }


    public TemplateKeywordGet getTemplatesByKeyword(TemplateKeywordType keyword, int pageNum, int limit) {

        Pageable pageable = PageRequest.of(pageNum, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<TemplateByKeywordResponse> result = templateKeywordRepository.findByKeywordOrderByTemplateCreatedAtDesc(keyword, pageable);

        if (result.isEmpty()) {
            throw new TemplateManagerExceptionHandler(TemplateManagerErrorCode.TEMPLATE_NOT_FOUND_BY_KEYWORD);
        }

        PageInfo pageInfo = PageInfo.of(
                pageNum,
                limit,
                result.getTotalPages(),
                result.getTotalElements()
        );

        return new TemplateKeywordGet(result.getContent(), pageInfo);
    }

}
