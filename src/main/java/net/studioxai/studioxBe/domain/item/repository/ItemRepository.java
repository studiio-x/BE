package net.studioxai.studioxBe.domain.item.repository;

import net.studioxai.studioxBe.domain.item.entity.Category;
import net.studioxai.studioxBe.domain.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findAll(Pageable pageable);

    Page<Item> findByCategory(Category category, Pageable pageable);
}
