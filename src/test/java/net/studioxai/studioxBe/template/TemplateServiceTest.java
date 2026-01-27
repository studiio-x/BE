package net.studioxai.studioxBe.template;

import net.studioxai.studioxBe.domain.template.dto.response.KeywordTemplatesResponse;
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
    @DisplayName("[getTemplatesByKeywords] 정상 조회")
    void getTemplatesByKeywords_ok() {
        TemplateKeywordType keyword = TemplateKeywordType.GENERAL_DISPLAY;

        TemplateByKeywordResponse dto =
                new TemplateByKeywordResponse(
                        1L,
                        keyword,
                        "https://dummy.com/img1.jpg",
                        Category.IMAGE
                );

        given(templateKeywordRepository.findByKeywordOrderByTemplateCreatedAtDesc(
                any(),
                any()
        )).willReturn(new PageImpl<>(List.of(dto)));

        List<KeywordTemplatesResponse> result =
                templateService.getTemplatesByKeywords(List.of(keyword), 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).keyword()).isEqualTo(keyword);
        assertThat(result.get(0).templates())
                .hasSize(1)
                .extracting("templateId", "imageUrl")
                .containsExactly(tuple(1L, "https://dummy.com/img1.jpg"));
    }


    @Test
    @DisplayName("[getTemplatesByKeywords] 템플릿이 없어도 키워드는 내려준다")
    void getTemplatesByKeywords_emptyTemplates() {
        TemplateKeywordType keyword = TemplateKeywordType.GENERAL_DISPLAY;

        given(templateKeywordRepository.findByKeywordOrderByTemplateCreatedAtDesc(
                any(),
                any()
        )).willReturn(Page.empty());

        List<KeywordTemplatesResponse> result =
                templateService.getTemplatesByKeywords(List.of(keyword), 10);

        assertThat(result).isEmpty();

    }


}
