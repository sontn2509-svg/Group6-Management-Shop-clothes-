package com.group6.clothingstore.product.service;

import com.group6.clothingstore.product.dto.CategoryFormDto;
import com.group6.clothingstore.product.entity.Category;
import com.group6.clothingstore.product.repository.CategoryRepository;
import com.group6.clothingstore.product.repository.ProductRepository;
import com.group6.clothingstore.shared.enums.CategoryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByCategoryIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {
        return categoryRepository.findByStatusOrderByCategoryNameAsc(CategoryStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại!"));
    }

    @Transactional
    public Category createCategory(CategoryFormDto dto) {
        String name = dto.getCategoryName().trim();
        if (categoryRepository.existsByCategoryNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Tên danh mục '" + name + "' đã tồn tại!");
        }

        Category category = Category.builder()
                .categoryName(name)
                .description(dto.getDescription() != null ? dto.getDescription().trim() : null)
                .status(dto.getStatus() != null ? dto.getStatus() : CategoryStatus.ACTIVE)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Admin tạo danh mục mới: id={}, name={}", saved.getCategoryId(), saved.getCategoryName());
        return saved;
    }

    @Transactional
    public Category updateCategory(Integer id, CategoryFormDto dto) {
        Category category = getCategoryById(id);
        String name = dto.getCategoryName().trim();

        if (categoryRepository.existsByCategoryNameIgnoreCaseAndCategoryIdNot(name, id)) {
            throw new IllegalArgumentException("Tên danh mục '" + name + "' đã được sử dụng!");
        }

        category.setCategoryName(name);
        category.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        if (dto.getStatus() != null) {
            category.setStatus(dto.getStatus());
        }

        log.info("Admin cập nhật danh mục: id={}, name={}", id, name);
        return categoryRepository.save(category);
    }

    /**
     * BR05 & ERR-11: Không được xóa danh mục (Category) đang chứa sản phẩm.
     */
    @Transactional
    public void deleteCategory(Integer id) {
        Category category = getCategoryById(id);

        if (productRepository.existsByCategoryCategoryId(id)) {
            throw new IllegalArgumentException(
                    "Không thể xóa danh mục đang chứa sản phẩm. Hãy chuyển sản phẩm sang danh mục khác trước!");
        }

        categoryRepository.delete(category);
        log.info("Admin xóa danh mục: id={}, name={}", id, category.getCategoryName());
    }
}
