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
    public List<ItemGet> getItems(
            @RequestParam(required = false)
            String category
    ){
        return itemService.itemGets(category);
    }
}
