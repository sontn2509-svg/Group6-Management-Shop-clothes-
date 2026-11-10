package com.group6.clothingstore.product.dto;

import com.group6.clothingstore.shared.enums.ProductGender;
import com.group6.clothingstore.shared.enums.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFormDto {

    private Long productId;

    @NotBlank(message = "Tên sản phẩm không được để trống!")
    private String productName;

    @NotNull(message = "Vui lòng chọn danh mục!")
    private Integer categoryId;

    @NotNull(message = "Giá bán không được để trống!")
    @DecimalMin(value = "0.01", message = "Giá bán phải lớn hơn 0!")
    private BigDecimal price;

    @NotNull(message = "Số lượng tồn kho không được để trống!")
    @Min(value = 0, message = "Số lượng tồn kho không được là số âm!")
    private Integer stockQuantity;

    private List<String> sizes;

    @NotBlank(message = "Màu sắc không được để trống!")
    private String color;

    private String material;

    @NotNull(message = "Vui lòng chọn giới tính!")
    private ProductGender gender;

    private String description;

    private String imageUrl;

    private ProductStatus status;
}
