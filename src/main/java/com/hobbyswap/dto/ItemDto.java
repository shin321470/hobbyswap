package com.hobbyswap.dto;

public class ItemDto {
    private Long id;
    private String title;
    private Double price;

    public ItemDto(Long id, String title, Double price) {
        this.id = id;
        this.title = title;
        this.price = price;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
