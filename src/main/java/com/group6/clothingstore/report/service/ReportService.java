package com.group6.clothingstore.report.service;

import com.group6.clothingstore.order.repository.OrderRepository;
import com.group6.clothingstore.product.repository.ProductRepository;
import com.group6.clothingstore.report.dto.DashboardKpiDto;
import com.group6.clothingstore.report.dto.MonthlyRevenueDto;
import com.group6.clothingstore.report.dto.TopSellingProductDto;
import com.group6.clothingstore.shared.enums.ProductStatus;
import com.group6.clothingstore.shared.enums.Role;
import com.group6.clothingstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * FR25: Lấy các chỉ số KPI cho Dashboard
     */
    @Transactional(readOnly = true)
    public DashboardKpiDto getDashboardKpi() {
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        long pendingOrders = orderRepository.countOrdersNeedingProcessing();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long activeProducts = productRepository.countByStatus(ProductStatus.ACTIVE);

        return DashboardKpiDto.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .pendingOrdersCount(pendingOrders)
                .totalCustomersCount(totalCustomers)
                .activeProductsCount(activeProducts)
                .build();
    }

    /**
     * FR23 & BR07: Báo cáo doanh thu theo tháng (từ các đơn DELIVERED)
     */
    @Transactional(readOnly = true)
    public List<MonthlyRevenueDto> getMonthlyRevenueReport() {
        List<Object[]> rows = orderRepository.getMonthlyRevenueNative();
        List<MonthlyRevenueDto> list = new ArrayList<>();

        for (Object[] r : rows) {
            Integer year = ((Number) r[0]).intValue();
            Integer month = ((Number) r[1]).intValue();
            BigDecimal revenue = r[2] != null ? new BigDecimal(r[2].toString()) : BigDecimal.ZERO;
            Long orderCount = ((Number) r[3]).longValue();

            list.add(MonthlyRevenueDto.builder()
                    .year(year)
                    .month(month)
                    .revenue(revenue)
                    .orderCount(orderCount)
                    .build());
        }

        return list;
    }

    /**
     * FR24: Top 5 mẫu quần áo bán chạy nhất (từ các đơn DELIVERED)
     */
    @Transactional(readOnly = true)
    public List<TopSellingProductDto> getTopSellingProducts() {
        List<Object[]> rows = orderRepository.getTopSellingProductsNative();
        List<TopSellingProductDto> list = new ArrayList<>();

        for (Object[] r : rows) {
            Long productId = ((Number) r[0]).longValue();
            String productName = (String) r[1];
            String imageUrl = (String) r[2];
            BigDecimal price = r[3] != null ? new BigDecimal(r[3].toString()) : BigDecimal.ZERO;
            Long totalSold = ((Number) r[4]).longValue();
            BigDecimal totalRevenue = r[5] != null ? new BigDecimal(r[5].toString()) : BigDecimal.ZERO;

            list.add(TopSellingProductDto.builder()
                    .productId(productId)
                    .productName(productName)
                    .imageUrl(imageUrl)
                    .price(price)
                    .totalQuantitySold(totalSold)
                    .totalRevenue(totalRevenue)
                    .build());
        }

        return list;
    }
}
