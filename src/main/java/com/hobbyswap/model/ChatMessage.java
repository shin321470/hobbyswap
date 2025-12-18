package com.hobbyswap.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity // 1. 標記為資料庫實體
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String content;
    private String type; // CHAT, JOIN, LEAVE

    private LocalDateTime timestamp; // 2. 加入時間，為了排序

    // ▼ 新增：收件人 (若為 null 代表是公開訊息，若有值代表是私訊)
    private String recipient;

    // 建構子、Getter、Setter
    public ChatMessage() {
        this.timestamp = LocalDateTime.now(); // 預設當下時間
    }

    // ... 以下是 Getter/Setter ...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
}