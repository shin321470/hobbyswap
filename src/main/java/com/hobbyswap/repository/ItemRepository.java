package com.hobbyswap.repository;

import com.hobbyswap.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    // 找出所有特定狀態的商品 (例如只找 ON_SALE 的)，並按上架時間倒序排列
    List<Item> findByStatusOrderByUploadDateDesc(String status);
}