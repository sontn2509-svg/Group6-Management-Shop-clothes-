
package com.group6.clothingstore.product.repository;

import com.group6.clothingstore.product.entity.Product;
import com.group6.clothingstore.shared.enums.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository
        extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    // FR11: Lay san pham moi nhat
    List<Product> findByStatusOrderByCreatedAtDesc(
            ProductStatus status,
            Pageable pageable
    );

    // FR11: Lay 8 san pham ban chay
    @Query(value = """
            SELECT TOP (8) d.product_id
            FROM OrderDetails d
            JOIN Orders o ON o.order_id = d.order_id
            JOIN Products p ON p.product_id = d.product_id
            WHERE o.order_status = 'DELIVERED'
              AND p.status = 'ACTIVE'
            GROUP BY d.product_id
            ORDER BY SUM(d.quantity) DESC
            """, nativeQuery = true)
    List<Long> findBestSellerIds();
}
