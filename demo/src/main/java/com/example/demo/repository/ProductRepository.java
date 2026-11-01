package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p where (:name = '' or lower(p.name) like lower(concat('%', :name, '%'))) and (:categoryId is null or p.category.id = :categoryId)")
    Page<Product> search(@Param("name") String name, @Param("categoryId") Long categoryId, Pageable pageable);
}
