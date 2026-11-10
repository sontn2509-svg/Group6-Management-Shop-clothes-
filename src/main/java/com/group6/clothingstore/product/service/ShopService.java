package com.group6.clothingstore.product.service;

import com.group6.clothingstore.product.entity.Category;
import com.group6.clothingstore.product.entity.Product;
import com.group6.clothingstore.product.repository.CategoryRepository;
import com.group6.clothingstore.product.repository.ProductRepository;
import com.group6.clothingstore.shared.enums.CategoryStatus;
import com.group6.clothingstore.shared.enums.ProductGender;
import com.group6.clothingstore.shared.enums.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopService {
    private final ProductRepository products;
    private final CategoryRepository categories;
    public List<Category> categories() { return categories.findByStatusOrderByCategoryNameAsc(CategoryStatus.ACTIVE); }
    public List<Product> newest() { return products.findByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE, PageRequest.of(0, 8)); }
    public List<Product> bestSellers() {
        List<Long> ids = products.findBestSellerIds();
        if (ids.isEmpty()) return newest();
        Map<Long, Product> byId = new HashMap<>();
        products.findAllById(ids).forEach(p -> byId.put(p.getProductId(), p));
        return ids.stream().map(byId::get).filter(Objects::nonNull).toList();
    }
    public Product detail(long id) {
        return products.findById(id).filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm"));
    }
    public Page<Product> search(String keyword, Integer categoryId, String gender,
                                String size, BigDecimal minPrice, BigDecimal maxPrice, int page) {
        Specification<Product> filter = (root, query, cb) -> cb.equal(root.get("status"), ProductStatus.ACTIVE);
        if (keyword != null && !keyword.isBlank()) {
            String q = "%" + keyword.strip().toLowerCase(Locale.ROOT) + "%";
            filter = filter.and((root, query, cb) -> cb.like(cb.lower(root.get("productName")), q));
        }
        if (categoryId != null) filter = filter.and((root, query, cb) -> cb.equal(root.get("category").get("categoryId"), categoryId));
        if (gender != null && !gender.isBlank()) {
            ProductGender g;
            try { g = ProductGender.valueOf(gender.toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Giới tính không hợp lệ"); }
            ProductGender chosen = g;
            filter = filter.and((root, query, cb) -> cb.equal(root.get("gender"), chosen));
        }
        if (size != null && !size.isBlank()) {
            String selected = size.strip().toUpperCase(Locale.ROOT);
            if (!List.of("S", "M", "L", "XL").contains(selected)) throw new IllegalArgumentException("Size không hợp lệ");
            filter = filter.and((root, query, cb) -> {
                // Store stores sizes as CSV: S,M,L,XL. Remove whitespace before exact-token lookup.
                var normalized = cb.function("replace", String.class, root.get("availableSizes"), cb.literal(" "), cb.literal(""));
                return cb.like(cb.concat(cb.concat(",", normalized), ","), "%," + selected + ",%");
            });
        }
        if (minPrice != null) filter = filter.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        if (maxPrice != null) filter = filter.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        return products.findAll(filter, PageRequest.of(Math.max(page, 0), 12, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "productId"))));
    }
}
