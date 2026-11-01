package com.example.demo.model;

import com.example.demo.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryForm {
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 255, message = "Tên danh mục tối đa 255 ký tự")
    private String name;
    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    public CategoryForm() {}
    public CategoryForm(Category category) {
        name = category.getName();
        description = category.getDescription();
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
