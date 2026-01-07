package net.studioxai.studioxBe.domain.template.repository;

import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.global.entity.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    List<Template> findByCategory(Category category);
}
