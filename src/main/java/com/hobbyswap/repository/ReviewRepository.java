package com.hobbyswap.repository;

import com.hobbyswap.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 根據商品 ID 尋找評論
    List<Review> findByItemId(Long itemId);
}