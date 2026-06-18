package com.hobbyswap.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "商品標題不可空白")
    private String title;

    @Size(max = 1000, message = "描述長度不可超過 1000 字")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "請輸入價格")
    @PositiveOrZero(message = "價格不可為負數")
    private Double price;

    private String imageName;

    @PositiveOrZero(message = "庫存數量不可為負數")
    private Integer stockQuantity = 1;

    // 狀態 (以 enum 表示，存入資料庫時為字串)
    @Enumerated(EnumType.STRING)
    private ItemStatus status = ItemStatus.ON_SALE;

    private LocalDateTime uploadDate;

    private String category;

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
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public Integer getStockQuantity() { return stockQuantity;}
    public void setStockQuantity(Integer stockQuantity) {this.stockQuantity = stockQuantity;}
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}