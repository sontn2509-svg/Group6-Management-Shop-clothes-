package com.group6.clothingstore.review.repository;

import com.group6.clothingstore.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductProductIdAndIsVisibleTrueOrderByCreatedAtDesc(Long productId);

    List<Review> findAllByOrderByCreatedAtDesc();

    List<Review> findByProductProductIdOrderByCreatedAtDesc(Long productId);

    List<Review> findByIsVisibleOrderByCreatedAtDesc(Boolean isVisible);

    @Query("SELECT AVG(CAST(r.rating as double)) FROM Review r WHERE r.product.productId = :productId AND r.isVisible = true")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    long countByProductProductIdAndIsVisibleTrue(Long productId);
}
