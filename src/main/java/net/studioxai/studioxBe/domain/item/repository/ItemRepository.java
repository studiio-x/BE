package net.studioxai.studioxBe.domain.item.repository;

import net.studioxai.studioxBe.global.entity.enums.Category;
import net.studioxai.studioxBe.domain.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findAll(Pageable pageable);

    Page<Item> findByCategory(Category category, Pageable pageable);
}
