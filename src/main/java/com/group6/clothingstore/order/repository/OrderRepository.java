package com.group6.clothingstore.order.repository;

import com.group6.clothingstore.order.entity.Order;
import com.group6.clothingstore.shared.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserUserIdOrderByOrderDateDesc(Long userId);

    List<Order> findByOrderStatusOrderByOrderDateDesc(OrderStatus orderStatus);

    List<Order> findAllByOrderByOrderDateDesc();

    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);

    Page<Order> findByOrderStatusOrderByOrderDateDesc(OrderStatus orderStatus, Pageable pageable);

    // BR07 & FR25: Báo cáo thống kê doanh thu chỉ tính tổng tiền của các đơn DELIVERED
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderStatus = com.group6.clothingstore.shared.enums.OrderStatus.DELIVERED")
    BigDecimal calculateTotalRevenue();

    // FR25: Đơn mới cần xử lý / đóng gói (PENDING hoặc CONFIRMED)
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus IN (com.group6.clothingstore.shared.enums.OrderStatus.PENDING, com.group6.clothingstore.shared.enums.OrderStatus.CONFIRMED)")
    long countOrdersNeedingProcessing();

    // FR23: Thống kê doanh thu theo tháng từ các đơn DELIVERED
    @Query(value = """
            SELECT YEAR(order_date) as [order_year], MONTH(order_date) as [order_month],
                   SUM(total_amount) as [revenue], COUNT(order_id) as [order_count]
            FROM Orders
            WHERE order_status = 'DELIVERED'
            GROUP BY YEAR(order_date), MONTH(order_date)
            ORDER BY [order_year] DESC, [order_month] DESC
            """, nativeQuery = true)
    List<Object[]> getMonthlyRevenueNative();

    // FR24: Top 5 mẫu quần áo bán chạy nhất (từ các đơn DELIVERED)
    @Query(value = """
            SELECT TOP (5) p.product_id, p.product_name, p.image_url, p.price,
                   SUM(d.quantity) as total_sold,
                   SUM(d.quantity * d.unit_price) as total_revenue
            FROM OrderDetails d
            JOIN Orders o ON o.order_id = d.order_id
            JOIN Products p ON p.product_id = d.product_id
            WHERE o.order_status = 'DELIVERED'
            GROUP BY p.product_id, p.product_name, p.image_url, p.price
            ORDER BY total_sold DESC
            """, nativeQuery = true)
    List<Object[]> getTopSellingProductsNative();
}