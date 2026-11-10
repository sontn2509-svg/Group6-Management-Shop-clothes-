package com.group6.clothingstore.report.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyRevenueDto {
    private Integer year;
    private Integer month;
    private BigDecimal revenue;
    private Long orderCount;
}
