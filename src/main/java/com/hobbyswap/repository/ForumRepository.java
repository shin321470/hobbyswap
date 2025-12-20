package com.hobbyswap.repository;

import com.hobbyswap.model.ForumCategory;
import com.hobbyswap.model.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ForumRepository extends JpaRepository<ForumPost, Long> {
    // 根據分類找文章 (最新的在前面)
    List<ForumPost> findByCategoryOrderByCreatedAtDesc(ForumCategory category);

    // 找所有文章 (最新的在前面)
    List<ForumPost> findAllByOrderByCreatedAtDesc();
}