package net.studioxai.studioxBe.domain.template.repository;

import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.domain.template.entity.TemplateKeyword;
import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemplateKeywordRepository
        extends JpaRepository<TemplateKeyword, Long> {

    @Query("""
        select distinct tk.template
        from TemplateKeyword tk
        where tk.keyword = :keyword
    """)
    List<Template> findTemplatesByKeyword(
            @Param("keyword") TemplateKeywordType keyword
    );
}

