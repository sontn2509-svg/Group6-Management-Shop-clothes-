package com.group6.clothingstore.product.entity;

import com.group6.clothingstore.shared.enums.CategoryStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name", nullable = false, unique = true, length = 100)
    private String categoryName;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CategoryStatus status;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = CategoryStatus.ACTIVE;
        }
    }
}
