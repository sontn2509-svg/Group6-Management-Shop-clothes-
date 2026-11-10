
package com.group6.clothingstore.product.repository;

import com.group6.clothingstore.product.entity.Category;
import com.group6.clothingstore.shared.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository
        extends JpaRepository<Category, Integer> {

    List<Category> findByStatusOrderByCategoryNameAsc(
            CategoryStatus status
    );

    List<Category> findAllByOrderByCategoryIdDesc();

    boolean existsByCategoryNameIgnoreCase(String categoryName);

    boolean existsByCategoryNameIgnoreCaseAndCategoryIdNot(String categoryName, Integer categoryId);
}
