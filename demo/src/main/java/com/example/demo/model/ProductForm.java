package com.example.demo.model;

import com.example.demo.entity.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductForm {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
    private String name;
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private Double price;
    @NotNull(message = "Tồn kho không được để trống")
    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stock;
    @NotNull(message = "Hãy chọn danh mục")
    private Long categoryId;

    public ProductForm() {}
    public ProductForm(Product product) {
        name = product.getName();
        price = product.getPrice();
        stock = product.getStock();
        categoryId = product.getCategory().getId();
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
