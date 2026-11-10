package com.group6.clothingstore.report.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardKpiDto {
    private BigDecimal totalRevenue;
    private long pendingOrdersCount;
    private long totalCustomersCount;
    private long activeProductsCount;
}
