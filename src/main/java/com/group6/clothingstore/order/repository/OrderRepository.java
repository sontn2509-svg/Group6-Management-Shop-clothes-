package com.group6.clothingstore.order.repository;

import com.group6.clothingstore.order.entity.Order;
import com.group6.clothingstore.shared.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserUserIdOrderByOrderDateDesc(Long userId);

    List<Order> findByOrderStatusOrderByOrderDateDesc(OrderStatus orderStatus);

    List<Order> findAllByOrderByOrderDateDesc();
}