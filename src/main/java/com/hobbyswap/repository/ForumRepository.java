package com.hobbyswap.repository;

import com.hobbyswap.model.ForumCategory;
import com.hobbyswap.model.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ForumRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content);

    List<ForumPost> findByCategory(ForumCategory category);
}