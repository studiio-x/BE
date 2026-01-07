package net.studioxai.studioxBe.domain.template.repository;

import net.studioxai.studioxBe.domain.template.dto.response.TemplateByCategoryResponse;
import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.global.entity.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    @Query("""
        select new net.studioxai.studioxBe.domain.template.dto.response.TemplateByCategoryResponse(
            t.id,
            t.imageUrl
        )
        from Template t
        where t.category = :category
        order by t.createdAt desc
    """)
    List<TemplateByCategoryResponse> findByCategoryOrderByCreatedAtDesc(@Param("category") Category category);
}
