package com.hobbyswap.repository;

import com.hobbyswap.model.Cart;
import com.hobbyswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // 透過使用者找購物車
    Cart findByUser(User user);
}