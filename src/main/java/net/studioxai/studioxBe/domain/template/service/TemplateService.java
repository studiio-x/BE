package net.studioxai.studioxBe.domain.template.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateCategoryGet;
import net.studioxai.studioxBe.domain.template.dto.response.KeywordTemplatesResponse;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateByCategoryResponse;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateByKeywordResponse;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateKeywordResponse;
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


import java.util.ArrayList;
import java.util.Arrays;
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
                                t.getImageObjectKey()
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

    public List<TemplateKeywordResponse> getAllTemplateKeywords() {
        return Arrays.stream(TemplateKeywordType.values())
                .map(TemplateKeywordResponse::new)
                .toList();
    }

    public List<KeywordTemplatesResponse> getTemplatesByKeywords(List<TemplateKeywordType> keywords, int limitPerKeyword) {

        List<KeywordTemplatesResponse> result = new ArrayList<>();

        for (TemplateKeywordType keyword : keywords) {

            Pageable pageable = PageRequest.of(
                    0,
                    limitPerKeyword
            );

            Page<TemplateByKeywordResponse> page =
                    templateKeywordRepository.findByKeywordOrderByTemplateCreatedAtDesc(keyword, pageable);

            if (page.isEmpty()) {
                continue; // 해당 키워드에 템플릿 없으면 스킵
            }

            result.add(
                    new KeywordTemplatesResponse(
                            keyword,
                            page.getContent()
                    )
            );
        }

        return result;
    }

    public List<TemplateByKeywordResponse> searchTemplatesByKeyword(String searchText) {

        if (searchText == null || searchText.isBlank()) {
            return List.of();
        }

        TemplateKeywordType keywordType = TemplateKeywordType
                .findByTitleLike(searchText)
                .orElseThrow(() -> new TemplateManagerExceptionHandler(TemplateManagerErrorCode.TEMPLATE_NOT_FOUND_BY_KEYWORD)
                );

        return templateKeywordRepository.searchByKeyword(keywordType);
    }

}
