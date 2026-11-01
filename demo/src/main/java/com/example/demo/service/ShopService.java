package com.example.demo.service;

import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.model.CategoryForm;
import com.example.demo.model.ProductForm;
import com.example.demo.model.ProductResponse;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ShopService {
    private final CategoryRepository categories;
    private final ProductRepository products;

    public ShopService(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<Category> categories() { return categories.findAll(Sort.by("name")); }

    @Transactional(readOnly = true)
    public Category category(Long id) {
        return categories.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));
    }

    @Transactional
    public Category saveCategory(Long id, CategoryForm form) {
        Category category = id == null ? new Category() : category(id);
        category.setName(form.getName().trim());
        category.setDescription(form.getDescription());
        return categories.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = category(id);
        if (products.existsByCategoryId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Danh mục còn sản phẩm; hãy xóa hoặc chuyển sản phẩm trước");
        }
        categories.delete(category);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> products(String name, Long categoryId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phân trang không hợp lệ");
        }
        return products.search(name == null ? "" : name.trim(), categoryId,
                PageRequest.of(page, size, Sort.by("id").descending())).map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse product(Long id) { return ProductResponse.from(findProduct(id)); }

    @Transactional(readOnly = true)
    public ProductForm productForm(Long id) { return new ProductForm(findProduct(id)); }

    private Product findProduct(Long id) {
        return products.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
    }

    @Transactional
    public ProductResponse saveProduct(Long id, ProductForm form) {
        Product product = id == null ? new Product() : findProduct(id);
        Category category = category(form.getCategoryId());
        product.setName(form.getName().trim());
        product.setPrice(form.getPrice());
        product.setStock(form.getStock());
        product.setCategory(category);
        return ProductResponse.from(products.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) { products.delete(findProduct(id)); }
}
