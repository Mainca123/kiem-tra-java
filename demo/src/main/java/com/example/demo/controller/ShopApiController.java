package com.example.demo.controller;

import com.example.demo.entity.Category;
import com.example.demo.model.CategoryForm;
import com.example.demo.model.PageResponse;
import com.example.demo.model.ProductForm;
import com.example.demo.model.ProductResponse;
import com.example.demo.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ShopApiController {
    private final ShopService shop;
    public ShopApiController(ShopService shop) { this.shop = shop; }

    @GetMapping("/categories")
    public List<Category> categories() { return shop.categories(); }

    @GetMapping("/categories/{id}")
    public Category category(@PathVariable Long id) { return shop.category(id); }

    @PostMapping("/categories") @ResponseStatus(HttpStatus.CREATED)
    public Category createCategory(@Valid @RequestBody CategoryForm form) { return shop.saveCategory(null, form); }

    @PutMapping("/categories/{id}")
    public Category updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryForm form) {
        return shop.saveCategory(id, form);
    }

    @DeleteMapping("/categories/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) { shop.deleteCategory(id); }

    @GetMapping("/products")
    public PageResponse<ProductResponse> products(@RequestParam(defaultValue = "") String name,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return PageResponse.from(shop.products(name, categoryId, page, size));
    }

    @GetMapping("/products/{id}")
    public ProductResponse product(@PathVariable Long id) { return shop.product(id); }

    @PostMapping("/products") @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductForm form) { return shop.saveProduct(null, form); }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductForm form) {
        return shop.saveProduct(id, form);
    }

    @DeleteMapping("/products/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) { shop.deleteProduct(id); }
}
