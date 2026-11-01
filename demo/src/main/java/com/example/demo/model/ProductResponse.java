package com.example.demo.model;

import com.example.demo.entity.Product;

public record ProductResponse(Long id, String name, Double price, Integer stock,
                              Long categoryId, String categoryName) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getStock(),
                product.getCategory().getId(), product.getCategory().getName());
    }
}
