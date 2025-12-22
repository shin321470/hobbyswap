package com.hobbyswap.repository;

import com.hobbyswap.model.ForumComment;
import com.hobbyswap.model.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    List<ForumComment> findByPostOrderByCreatedAtAsc(ForumPost post);
    List<ForumComment> findByPostAndParentIsNullOrderByCreatedAtAsc(ForumPost post);
}