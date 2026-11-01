package com.example.demo;

import com.example.demo.entity.Category;
import com.example.demo.model.CategoryForm;
import com.example.demo.service.ShopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:shoptest;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class ShopApplicationTests {
    @Autowired private WebApplicationContext context;
    @Autowired private ShopService shop;
    private MockMvc mvc;

    @BeforeEach
    void setup() { mvc = MockMvcBuilders.webAppContextSetup(context).build(); }

    @Test
    void categoryAndProductCrudSearchAndFilter() throws Exception {
        CategoryForm form = new CategoryForm();
        form.setName("Máy tính");
        Category category = shop.saveCategory(null, form);
        long categoryId = category.getId();

        mvc.perform(post("/api/products").contentType("application/json")
                .content("{\"name\":\"Laptop Pro\",\"price\":1200,\"stock\":5,\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.categoryName").value("Máy tính"));
        mvc.perform(get("/api/products").param("name", "laptop").param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/products").param("categoryId", "999999"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        long productId = shop.products("", null, 0, 10).getContent().get(0).id();
        mvc.perform(put("/api/products/{id}", productId).contentType("application/json")
                .content("{\"name\":\"Laptop Air\",\"price\":999,\"stock\":2,\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Laptop Air"));
        mvc.perform(delete("/api/categories/{id}", categoryId)).andExpect(status().isConflict());
        mvc.perform(delete("/api/products/{id}", productId)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/categories/{id}", categoryId)).andExpect(status().isNoContent());
        mvc.perform(get("/api/products/{id}", productId)).andExpect(status().isNotFound());
    }

    @Test
    void validationAndMissingCategoryAreRejected() throws Exception {
        mvc.perform(post("/api/categories").contentType("application/json").content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.name").exists());
        mvc.perform(post("/api/products").contentType("application/json")
                .content("{\"name\":\" \",\"price\":0,\"stock\":-1,\"categoryId\":1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.name").exists())
                .andExpect(jsonPath("$.fields.price").exists()).andExpect(jsonPath("$.fields.stock").exists());
        mvc.perform(post("/api/products").contentType("application/json")
                .content("{\"name\":\"Valid\",\"price\":1,\"stock\":0,\"categoryId\":999999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void thymeleafPagesAndFormValidationWork() throws Exception {
        mvc.perform(get("/")) .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/products"));
        mvc.perform(get("/products")).andExpect(status().isOk()).andExpect(content().string(containsString("Quản lý sản phẩm")));
        mvc.perform(get("/categories/new")).andExpect(status().isOk()).andExpect(content().string(containsString("Tên danh mục")));
        mvc.perform(post("/categories").param("name", ""))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Tên danh mục không được để trống")));
        mvc.perform(post("/categories").param("name", "Điện tử"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/categories"));
        mvc.perform(get("/categories")).andExpect(status().isOk()).andExpect(content().string(containsString("Điện tử")));
        long categoryId = shop.categories().get(0).getId();
        mvc.perform(post("/categories/{id}", categoryId).param("name", "Máy tính"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/categories"));
        mvc.perform(get("/products/new")).andExpect(status().isOk()).andExpect(content().string(containsString("Máy tính")));
        mvc.perform(post("/products").param("name", "Laptop").param("price", "0")
                .param("stock", "-1").param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Giá phải lớn hơn 0")));
        mvc.perform(post("/products").param("name", "Laptop").param("price", "1200")
                .param("stock", "5").param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/products"));
        mvc.perform(get("/products").param("name", "Lap"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Laptop")));
    }

    @Test
    void metricsEndpointIsAvailable() throws Exception {
        mvc.perform(get("/actuator/prometheus")).andExpect(status().isOk())
                .andExpect(content().string(containsString("jvm_memory_used_bytes")));
    }
}
