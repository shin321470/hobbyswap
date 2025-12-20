package com.hobbyswap.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 一個使用者只有一個購物車
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 購物車裡面有很多項目 (CascadeType.ALL 代表刪除購物車時，裡面的項目也會一起刪除)
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    // 計算購物車總金額
    public Double getTotalPrice() {
        return items.stream()
                .mapToDouble(CartItem::getSubTotal)
                .sum();
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
}