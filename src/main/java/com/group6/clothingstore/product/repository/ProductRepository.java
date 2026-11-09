package com.group6.clothingstore.product.repository;

import com.group6.clothingstore.product.entity.Product;
import com.group6.clothingstore.shared.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStatus(ProductStatus status);
}