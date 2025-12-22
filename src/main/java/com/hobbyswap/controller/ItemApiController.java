package com.hobbyswap.controller;

import com.hobbyswap.dto.ItemDto;
import com.hobbyswap.model.Item;
import com.hobbyswap.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
public class ItemApiController {

    @Autowired
    private ItemService itemService;

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String keyword) {
        List<Item> items = itemService.searchItems(keyword);

        return items.stream()
                .map(item -> new ItemDto(
                        item.getId(),
                        item.getTitle(),
                        item.getPrice() != null ? item.getPrice().doubleValue() : 0.0 // 防止價格 null
                ))
                .collect(Collectors.toList());
    }
}
