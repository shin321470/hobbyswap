package com.hobbyswap.service;

import com.hobbyswap.model.Item;
import com.hobbyswap.model.User;
import com.hobbyswap.repository.ItemRepository;
import com.hobbyswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemService {

    @Autowired private ItemRepository itemRepository;
    @Autowired private UserRepository userRepository;

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
    @org.springframework.transaction.annotation.Transactional
    public void buyItem(Long itemId, int quantity) { // ★多加一個參數 quantity
        Item item = findById(itemId);

        if (quantity > 0 && item.getStockQuantity() >= quantity) {

            item.setStockQuantity(item.getStockQuantity() - quantity);

            if (item.getStockQuantity() == 0) {
                item.setStatus("SOLD");
            }

            itemRepository.save(item);
        } else {
            throw new RuntimeException("庫存不足或數量錯誤！");
        }
    }

    public void deleteItem(Long id) {
        Item item = findById(id);
        item.setStatus("DELETED");
        itemRepository.save(item);
    }

    public List<Item> searchItems(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return itemRepository.findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
                    "ON_SALE", keyword, "ON_SALE", keyword
            );
        }
        return findAllOnSale();
    }


    public void toggleFavorite(Long itemId, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);

        Item item = findById(itemId);

        if (user != null) {
            if (user.getFavoriteItems().contains(item)) {
                user.getFavoriteItems().remove(item);
            } else {
                user.getFavoriteItems().add(item);
            }
            userRepository.save(user);
        }
    }
}