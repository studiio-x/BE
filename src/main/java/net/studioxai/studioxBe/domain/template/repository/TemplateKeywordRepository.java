package net.studioxai.studioxBe.domain.template.repository;

import net.studioxai.studioxBe.domain.template.dto.response.TemplateByKeywordResponse;
import net.studioxai.studioxBe.domain.template.entity.TemplateKeyword;
import net.studioxai.studioxBe.domain.template.entity.TemplateKeywordType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TemplateKeywordRepository extends JpaRepository<TemplateKeyword, Long> {

    @Query("""
    select new net.studioxai.studioxBe.domain.template.dto.response.TemplateByKeywordResponse(
        t.id,
        tk.keyword,
        t.imageUrl,
        t.category
    )
    from TemplateKeyword tk
    join tk.template t
    where tk.keyword = :keyword
    order by t.createdAt desc
""")
    Page<TemplateByKeywordResponse> findByKeywordOrderByTemplateCreatedAtDesc(@Param("keyword") TemplateKeywordType keyword, Pageable pageable);

    @Query("""
        select new net.studioxai.studioxBe.domain.template.dto.response.TemplateByKeywordResponse(
            t.id,
            tk.keyword,
            t.imageUrl,
            t.category
        )
        from TemplateKeyword tk
        join tk.template t
        where tk.keyword = :keyword
    """)
    List<TemplateByKeywordResponse> searchByKeyword(@Param("keyword") TemplateKeywordType keyword
    );
}

