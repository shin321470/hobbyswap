package com.hobbyswap.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private Double price;

    // 狀態: ON_SALE (上架中), SOLD (已售出)
    private String status = "ON_SALE";

    private LocalDateTime uploadDate;

    // 與使用者的多對一關聯 (賣家)
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    // Getter 與 Setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
}