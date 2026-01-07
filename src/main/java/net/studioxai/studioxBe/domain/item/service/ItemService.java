package net.studioxai.studioxBe.domain.item.service;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.item.dto.ItemGet;
import net.studioxai.studioxBe.global.entity.enums.Category;
import net.studioxai.studioxBe.domain.item.entity.Item;
import net.studioxai.studioxBe.domain.item.repository.ItemRepository;
import net.studioxai.studioxBe.global.dto.PageInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    public ItemGet itemGets(String category, int pageNum, int limit) {
        PageRequest pageRequest = PageRequest.of(pageNum, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        if(category == null){
            Page<Item> items = itemRepository.findAll(pageRequest);
            PageInfo pageInfo = PageInfo.of(pageNum, limit, items.getTotalPages(), items.getTotalElements());
            List<String> urls = items.getContent().stream().map(Item::getUrl).toList();

            return new ItemGet(urls, pageInfo);

        }

        else {
            Category tag = Category.valueOf(category.toUpperCase());
            Page<Item> items = itemRepository.findByCategory(tag, pageRequest);
            PageInfo pageInfo = PageInfo.of(pageNum, limit, items.getTotalPages(), items.getTotalElements());
            List<String> urls = items.getContent().stream().map(Item::getUrl).toList();

            return new ItemGet(urls, pageInfo);
        }

    }
}
