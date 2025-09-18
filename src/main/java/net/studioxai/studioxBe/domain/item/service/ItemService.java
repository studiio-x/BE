package net.studioxai.studioxBe.domain.item.service;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.item.dto.ItemGet;
import net.studioxai.studioxBe.domain.item.entity.Category;
import net.studioxai.studioxBe.domain.item.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    public List<ItemGet> itemGets(String category) {
        if(category == null){
            return itemRepository.findAll().stream()
                    .map( item -> ItemGet.toDto(item.getUrl())).toList();
        }

        else {
            Category tag = Category.valueOf(category.toUpperCase());
            return itemRepository.findByCategory(tag).stream()
                    .map( item -> ItemGet.toDto(item.getUrl())).toList();
        }

    }
}
