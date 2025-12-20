package com.hobbyswap.service;

import com.hobbyswap.model.*;
import com.hobbyswap.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private ItemRepository itemRepository;

    // 取得使用者的購物車，如果沒有就幫他創建一個空的
    public Cart getOrCreateCart(User user) {
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }
        return cart;
    }

    @Transactional
    public void addToCart(User user, Long itemId, int quantity) {
        Cart cart = getOrCreateCart(user);
        Item item = itemRepository.findById(itemId).orElse(null);

        if (item != null) {
            // 檢查購物車裡是否已經有這個商品
            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(ci -> ci.getItem().getId().equals(itemId))
                    .findFirst();

            if (existingItem.isPresent()) {
                // 如果有，就加數量
                CartItem ci = existingItem.get();
                ci.setQuantity(ci.getQuantity() + quantity);
            } else {
                // 如果沒有，就新增一個項目
                CartItem newItem = new CartItem(cart, item, quantity);
                cart.getItems().add(newItem);
            }
            cartRepository.save(cart);
        }
    }

    @Transactional
    public void removeFromCart(User user, Long cartItemId) {
        Cart cart = getOrCreateCart(user);
        // 移除指定的項目
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}