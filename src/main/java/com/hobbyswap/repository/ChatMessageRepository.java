package com.hobbyswap.repository;

import com.hobbyswap.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 撈出所有訊息，並依照時間舊->新排序
    List<ChatMessage> findAllByOrderByTimestampAsc();

    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender = :user1 AND m.recipient = :user2) OR " +
            "(m.sender = :user2 AND m.recipient = :user1) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> findConversation(@Param("user1") String user1, @Param("user2") String user2);
}