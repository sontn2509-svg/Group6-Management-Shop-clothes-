package com.group6.clothingstore.order.entity;

import com.group6.clothingstore.shared.enums.OrderStatus;
import com.group6.clothingstore.shared.enums.PaymentMethod;
import com.group6.clothingstore.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 15)
    private String receiverPhone;

    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus orderStatus;

    @Column(name = "order_date", updatable = false)
    private LocalDateTime orderDate;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.orderDate == null) {
            this.orderDate = LocalDateTime.now();
        }
        if (this.paymentMethod == null) {
            this.paymentMethod = PaymentMethod.COD;
        }
        if (this.orderStatus == null) {
            this.orderStatus = OrderStatus.PENDING;
        }
    }
}
