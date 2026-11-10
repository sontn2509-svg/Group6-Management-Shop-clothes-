package com.group6.clothingstore.cart.repository;

import com.group6.clothingstore.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserUserId(Long userId);

    Optional<CartItem> findByUserUserIdAndProductProductIdAndSelectedSize(
            Long userId, Long productId, String selectedSize);

    void deleteByUserUserId(Long userId);
}