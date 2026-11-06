package com.group6.clothingstore.cart.entity;

import com.group6.clothingstore.product.entity.Product;
import com.group6.clothingstore.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CartItems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "selected_size", nullable = false, length = 10)
    private String selectedSize;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @PrePersist
    protected void onCreate() {
        if (this.quantity == null) {
            this.quantity = 1;
        }
    }
}
