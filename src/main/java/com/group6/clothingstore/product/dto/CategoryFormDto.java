package com.group6.clothingstore.product.dto;

import com.group6.clothingstore.shared.enums.CategoryStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryFormDto {

    private Integer categoryId;

    @NotBlank(message = "Tên danh mục không được để trống!")
    private String categoryName;

    private String description;

    private CategoryStatus status;
}
