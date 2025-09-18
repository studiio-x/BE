package net.studioxai.studioxBe.domain.item.repository;

import net.studioxai.studioxBe.domain.item.entity.Category;
import net.studioxai.studioxBe.domain.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAll();

    List<Item> findByCategory(Category category);
}
