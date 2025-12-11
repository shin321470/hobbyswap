package com.hobbyswap.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "users") // 'user' 是 SQL 保留字，必須改名為 'users' 避免衝突
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    // 與商品的一對多關聯
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private Set<Item> items;

    // Getter 與 Setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<Item> getItems() { return items; }
    public void setItems(Set<Item> items) { this.items = items; }
}