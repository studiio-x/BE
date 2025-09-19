package net.studioxai.studioxBe.domain.item.controller;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.item.dto.ItemGet;
import net.studioxai.studioxBe.domain.item.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("")
    public ItemGet getItems(
            @RequestParam(required = false)
            String category,
            @RequestParam(required = true)
            int pageNum,
            @RequestParam(required = true)
            int limit
    ){
        return itemService.itemGets(category, pageNum-1, limit);
    }
}
