package com.hobbyswap.repository;

import com.hobbyswap.model.ForumComment;
import com.hobbyswap.model.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    // 根據帖子搜尋評論，並依照時間舊->新排序 (讓對話有順序)
    List<ForumComment> findByPostOrderByCreatedAtAsc(ForumPost post);
}