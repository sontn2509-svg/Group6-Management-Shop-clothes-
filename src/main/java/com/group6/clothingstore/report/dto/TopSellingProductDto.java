package com.group6.clothingstore.report.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSellingProductDto {
    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal price;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;
}
