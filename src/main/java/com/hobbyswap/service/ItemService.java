package com.hobbyswap.service;

import com.hobbyswap.model.Item;
import com.hobbyswap.model.User;
import com.hobbyswap.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemService {

    @Autowired private ItemRepository itemRepository;

    // 找出所有上架中 (ON_SALE) 的商品
    public List<Item> findAllOnSale() {
        return itemRepository.findByStatusOrderByUploadDateDesc("ON_SALE");
    }

    // 根據 ID 查找商品，若找不到則拋出例外
    public Item findById(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new RuntimeException("找不到商品"));
    }

    // 刊登新商品
    public void createItem(Item item, User seller) {
        item.setSeller(seller);
        item.setUploadDate(LocalDateTime.now());
        item.setStatus("ON_SALE");
        itemRepository.save(item);
    }

    // 購買商品邏輯
    public void buyItem(Long itemId) {
        Item item = findById(itemId);
        item.setStatus("SOLD"); // 簡單購買邏輯：將狀態改為已售出
        itemRepository.save(item);
    }
}