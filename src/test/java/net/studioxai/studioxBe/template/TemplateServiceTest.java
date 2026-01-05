package net.studioxai.studioxBe.template;

import net.studioxai.studioxBe.domain.template.dto.response.TemplateResponse;
import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;
import net.studioxai.studioxBe.domain.template.repository.TemplateRepository;
import net.studioxai.studioxBe.domain.template.service.TemplateService;
import net.studioxai.studioxBe.global.entity.enums.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private TemplateService templateService;

    @Test
    @DisplayName("[getTemplatesByCategory] category에 해당하는 template이 있으면 TemplateResponse 리스트를 반환한다")
    void getTemplatesByCategory_ok() {
        // given
        Category category = Category.IMAGE;

        Template template = mock(Template.class);
        given(template.getId()).willReturn(1L);
        given(template.getCategory()).willReturn(category);
        given(template.getImageUrl()).willReturn("image-url");

        given(templateRepository.findByCategory(category))
                .willReturn(List.of(template));

        // when
        List<TemplateResponse> result =
                templateService.getTemplatesByCategory(category);

        // then
        assertThat(result)
                .hasSize(1)
                .extracting("id", "category", "imageUrl")
                .containsExactly(
                        tuple(1L, category, "image-url")
                );
    }

    @Test
    @DisplayName("[getTemplatesByCategory] category에 해당하는 template이 없으면 빈 리스트를 반환한다")
    void getTemplatesByCategory_empty() {
        // given
        Category category = Category.IMAGE;

        given(templateRepository.findByCategory(category))
                .willReturn(List.of());

        // when
        List<TemplateResponse> result =
                templateService.getTemplatesByCategory(category);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("[getTemplatesByKeyword] keyword에 해당하는 template이 있으면 TemplateResponse 리스트를 반환한다")
    void getTemplatesByKeyword_ok() {
        // given
        TemplateKeywordType keyword = TemplateKeywordType.OUTDOOR;

        Template template = mock(Template.class);
        given(template.getId()).willReturn(2L);
        given(template.getCategory()).willReturn(Category.IMAGE);
        given(template.getImageUrl()).willReturn("outdoor-image-url");

        given(templateRepository.findByKeyword(keyword))
                .willReturn(List.of(template));

        // when
        List<TemplateResponse> result =
                templateService.getTemplatesByKeyword(keyword);

        // then
        assertThat(result)
                .hasSize(1)
                .extracting("id", "imageUrl")
                .containsExactly(
                        tuple(2L, "outdoor-image-url")
                );
    }

    @Test
    @DisplayName("[getTemplatesByKeyword] keyword에 해당하는 template이 없으면 빈 리스트를 반환한다")
    void getTemplatesByKeyword_empty() {
        // given
        TemplateKeywordType keyword = TemplateKeywordType.OUTDOOR;

        given(templateRepository.findByKeyword(keyword))
                .willReturn(List.of());

        // when
        List<TemplateResponse> result =
                templateService.getTemplatesByKeyword(keyword);

        // then
        assertThat(result).isEmpty();
    }
}

