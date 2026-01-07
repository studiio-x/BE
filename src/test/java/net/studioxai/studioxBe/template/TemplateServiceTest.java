package net.studioxai.studioxBe.template;

import net.studioxai.studioxBe.domain.template.dto.response.TemplateByCategoryResponse;
import net.studioxai.studioxBe.domain.template.dto.response.TemplateByKeywordResponse;
import net.studioxai.studioxBe.domain.template.dto.TemplateCategoryGet;
import net.studioxai.studioxBe.domain.template.dto.TemplateKeywordGet;
import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;
import net.studioxai.studioxBe.domain.template.repository.TemplateKeywordRepository;
import net.studioxai.studioxBe.domain.template.repository.TemplateRepository;
import net.studioxai.studioxBe.domain.template.service.TemplateService;
import net.studioxai.studioxBe.domain.template.exception.TemplateManagerExceptionHandler;
import net.studioxai.studioxBe.global.entity.enums.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateKeywordRepository templateKeywordRepository;

    @InjectMocks
    private TemplateService templateService;

    /* =========================
     * CATEGORY
     * ========================= */

    @Test
    @DisplayName("[getTemplatesByCategory] 정상 조회 시 TemplateCategoryGet 반환")
    void getTemplatesByCategory_ok() {
        // given
        Category category = Category.IMAGE;
        int pageNum = 0;
        int limit = 10;

        Template template = mock(Template.class);
        given(template.getId()).willReturn(1L);
        given(template.getImageUrl()).willReturn("image-url");

        Page<Template> page = new PageImpl<>(
                List.of(template),
                PageRequest.of(pageNum, limit),
                1
        );

        given(templateRepository.findByCategoryOrderByCreatedAtDesc(
                eq(category),
                any(Pageable.class)
        )).willReturn(page);

        // when
        TemplateCategoryGet result =
                templateService.getTemplatesByCategory(category, pageNum, limit);

        // then
        assertThat(result.templates())
                .hasSize(1)
                .extracting("templateId", "imageUrl")
                .containsExactly(tuple(1L, "image-url"));

        assertThat(result.pageInfo())
                .extracting("pageNum", "limit", "totalPages", "totalElements")
                .containsExactly(0, 10, 1, 1L);
    }

    @Test
    @DisplayName("[getTemplatesByCategory] 결과가 없으면 예외 발생")
    void getTemplatesByCategory_empty() {
        // given
        Category category = Category.IMAGE;

        Page<Template> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        given(templateRepository.findByCategoryOrderByCreatedAtDesc(
                eq(category),
                any(Pageable.class)
        )).willReturn(emptyPage);

        // when & then
        assertThatThrownBy(() ->
                templateService.getTemplatesByCategory(category, 0, 10)
        ).isInstanceOf(TemplateManagerExceptionHandler.class);
    }

    /* =========================
     * KEYWORD
     * ========================= */

    @Test
    @DisplayName("[getTemplatesByKeyword] 정상 조회 시 TemplateKeywordGet 반환")
    void getTemplatesByKeyword_ok() {
        // given
        TemplateKeywordType keyword = TemplateKeywordType.OUTDOOR;
        int pageNum = 0;
        int limit = 5;

        TemplateByKeywordResponse dto =
                new TemplateByKeywordResponse(
                        2L,
                        keyword,
                        "아웃도어",
                        "image-url",
                        Category.IMAGE
                );

        Page<TemplateByKeywordResponse> page =
                new PageImpl<>(
                        List.of(dto),
                        PageRequest.of(pageNum, limit),
                        1
                );

        given(templateKeywordRepository.findByKeywordOrderByTemplateCreatedAtDesc(
                eq(keyword),
                any(Pageable.class)
        )).willReturn(page);

        // when
        TemplateKeywordGet result =
                templateService.getTemplatesByKeyword(keyword, pageNum, limit);

        // then
        assertThat(result.templates())
                .hasSize(1)
                .extracting("templateId", "keywordTitle", "imageUrl")
                .containsExactly(tuple(2L, "아웃도어", "image-url"));

        assertThat(result.pageInfo())
                .extracting("pageNum", "limit", "totalPages", "totalElements")
                .containsExactly(0, 5, 1, 1L);
    }

    @Test
    @DisplayName("[getTemplatesByKeyword] 결과가 없으면 예외 발생")
    void getTemplatesByKeyword_empty() {
        // given
        TemplateKeywordType keyword = TemplateKeywordType.OUTDOOR;

        Page<TemplateByKeywordResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);

        given(templateKeywordRepository.findByKeywordOrderByTemplateCreatedAtDesc(
                eq(keyword),
                any(Pageable.class)
        )).willReturn(emptyPage);

        // when & then
        assertThatThrownBy(() ->
                templateService.getTemplatesByKeyword(keyword, 0, 5)
        ).isInstanceOf(TemplateManagerExceptionHandler.class);
    }
}
