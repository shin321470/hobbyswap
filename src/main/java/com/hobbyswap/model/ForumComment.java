package com.hobbyswap.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "forum_comments")
public class ForumComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 評論內容

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author; // 評論者

    @ManyToOne
    @JoinColumn(name = "post_id")
    private ForumPost post; // 屬於哪一篇帖子

    public ForumComment() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ForumComment parent;

    // 子回覆列表 (誰在回覆我？)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC") // 讓回覆依照時間排序
    private List<ForumComment> replies;

    // Getter & Setter
    public ForumComment getParent() { return parent; }
    public void setParent(ForumComment parent) { this.parent = parent; }
    public List<ForumComment> getReplies() { return replies; }
    public void setReplies(List<ForumComment> replies) { this.replies = replies; }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public ForumPost getPost() { return post; }
    public void setPost(ForumPost post) { this.post = post; }
}