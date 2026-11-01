package com.example.demo.controller;

import com.example.demo.model.CategoryForm;
import com.example.demo.model.ProductForm;
import com.example.demo.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShopWebController {
    private final ShopService shop;
    public ShopWebController(ShopService shop) { this.shop = shop; }

    @GetMapping("/")
    public String home() { return "redirect:/products"; }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", shop.categories());
        return "categories/list";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        model.addAttribute("title", "Thêm danh mục");
        model.addAttribute("action", "/categories");
        return "categories/form";
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Long id, Model model) {
        model.addAttribute("categoryForm", new CategoryForm(shop.category(id)));
        model.addAttribute("title", "Sửa danh mục");
        model.addAttribute("action", "/categories/" + id);
        return "categories/form";
    }

    @PostMapping("/categories")
    public String createCategory(@Valid @ModelAttribute CategoryForm categoryForm, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Thêm danh mục");
            model.addAttribute("action", "/categories");
            return "categories/form";
        }
        shop.saveCategory(null, categoryForm);
        return "redirect:/categories";
    }

    @PostMapping("/categories/{id}")
    public String updateCategory(@PathVariable Long id, @Valid @ModelAttribute CategoryForm categoryForm,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Sửa danh mục");
            model.addAttribute("action", "/categories/" + id);
            return "categories/form";
        }
        shop.saveCategory(id, categoryForm);
        return "redirect:/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirect) {
        try { shop.deleteCategory(id); }
        catch (ResponseStatusException ex) {
            if (ex.getStatusCode() != HttpStatus.CONFLICT) throw ex;
            redirect.addFlashAttribute("error", ex.getReason());
        }
        return "redirect:/categories";
    }

    @GetMapping("/products")
    public String products(@RequestParam(defaultValue = "") String name,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(defaultValue = "0") int page, Model model) {
        Page<?> result = shop.products(name, categoryId, page, 10);
        model.addAttribute("products", result);
        model.addAttribute("categories", shop.categories());
        model.addAttribute("name", name);
        model.addAttribute("categoryId", categoryId);
        return "products/list";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("productForm", new ProductForm());
        productFormModel(model, "Thêm sản phẩm", "/products");
        return "products/form";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        model.addAttribute("productForm", shop.productForm(id));
        productFormModel(model, "Sửa sản phẩm", "/products/" + id);
        return "products/form";
    }

    @PostMapping("/products")
    public String createProduct(@Valid @ModelAttribute ProductForm productForm, BindingResult result, Model model) {
        if (result.hasErrors()) {
            productFormModel(model, "Thêm sản phẩm", "/products");
            return "products/form";
        }
        shop.saveProduct(null, productForm);
        return "redirect:/products";
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable Long id, @Valid @ModelAttribute ProductForm productForm,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            productFormModel(model, "Sửa sản phẩm", "/products/" + id);
            return "products/form";
        }
        shop.saveProduct(id, productForm);
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        shop.deleteProduct(id);
        return "redirect:/products";
    }

    private void productFormModel(Model model, String title, String action) {
        model.addAttribute("categories", shop.categories());
        model.addAttribute("title", title);
        model.addAttribute("action", action);
    }
}
