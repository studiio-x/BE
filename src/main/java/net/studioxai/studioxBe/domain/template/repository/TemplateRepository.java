package net.studioxai.studioxBe.domain.template.repository;

import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;
import net.studioxai.studioxBe.global.entity.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    List<Template> findByCategory(Category category);

    @Query("""
        select distinct t
        from Template t
        join t.templateKeywords tk
        where tk.keyword = :keyword
    """)
    List<Template> findByKeyword(@Param("keyword") TemplateKeywordType keyword);

}
