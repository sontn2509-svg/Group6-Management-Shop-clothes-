package com.group6.clothingstore.order.repository;

import com.group6.clothingstore.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    // BR05: Kiểm tra xem sản phẩm đã phát sinh trong đơn hàng chưa
    boolean existsByProductProductId(Long productId);

    // BR06: Chỉ khách hàng đã mua và có ít nhất 1 đơn DELIVERED chứa sản phẩm đó mới được đánh giá
    @Query("""
            SELECT COUNT(od) > 0
            FROM OrderDetail od
            WHERE od.order.user.userId = :userId
              AND od.product.productId = :productId
              AND od.order.orderStatus = com.group6.clothingstore.shared.enums.OrderStatus.DELIVERED
            """)
    boolean hasUserPurchasedAndDelivered(@Param("userId") Long userId, @Param("productId") Long productId);
}
