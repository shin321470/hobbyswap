package com.hobbyswap.repository;

import com.hobbyswap.model.Item;
import com.hobbyswap.model.ItemStatus;
import com.hobbyswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    // 找出所有特定狀態的商品 (例如只找 ON_SALE 的)，並按上架時間倒序排列
    List<Item> findByStatusOrderByUploadDateDesc(ItemStatus status);

    List<Item> findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
            ItemStatus status1, String title, ItemStatus status2, String description
    );

    List<Item> findBySellerAndStatus(User seller, ItemStatus status);
}