package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp mô hình dữ liệu sản phẩm.
 * Ánh xạ với cấu trúc tại nút products/{productId} trên Firebase.
 * Lớp này thuộc module sản phẩm (TV2), được sử dụng trong module yêu thích
 * để hiển thị thông tin sản phẩm.
 */
public class Product {
    public String productId;
    public String name;
    public String description;
    public double price;
    public double salePrice;
    public List<String> images;
    public int stock;
    public double avgRating;
    public boolean isActive;

    public Product() {
        this.images = new ArrayList<>();
        this.isActive = true;
    }
}
