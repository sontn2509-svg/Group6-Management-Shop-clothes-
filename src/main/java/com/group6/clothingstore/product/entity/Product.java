package com.group6.clothingstore.product.entity;

import com.group6.clothingstore.shared.enums.ProductGender;
import com.group6.clothingstore.shared.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "available_sizes", nullable = false, length = 50)
    private String availableSizes;

    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Column(name = "material", length = 100)
    private String material;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private ProductGender gender;

    @Lob
    @Column(name = "description", columnDefinition = "nvarchar(max)")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.stockQuantity == null) {
            this.stockQuantity = 0;
        }
        if (this.gender == null) {
            this.gender = ProductGender.UNISEX;
        }
        if (this.status == null) {
            this.status = ProductStatus.ACTIVE;
        }
    }
}
