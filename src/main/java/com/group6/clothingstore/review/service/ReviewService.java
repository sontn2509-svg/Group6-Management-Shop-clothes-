package com.group6.clothingstore.review.service;

import com.group6.clothingstore.order.repository.OrderDetailRepository;
import com.group6.clothingstore.product.entity.Product;
import com.group6.clothingstore.product.repository.ProductRepository;
import com.group6.clothingstore.review.dto.ReviewDto;
import com.group6.clothingstore.review.entity.Review;
import com.group6.clothingstore.review.repository.ReviewRepository;
import com.group6.clothingstore.user.entity.User;
import com.group6.clothingstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Review> getVisibleReviews(Long productId) {
        return reviewRepository.findByProductProductIdAndIsVisibleTrueOrderByCreatedAtDesc(productId);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : null;
    }

    @Transactional(readOnly = true)
    public long countReviews(Long productId) {
        return reviewRepository.countByProductProductIdAndIsVisibleTrue(productId);
    }

    /**
     * BR06: Kiểm tra điều kiện đánh giá sản phẩm (đã mua và có đơn DELIVERED chứa sản phẩm)
     */
    @Transactional(readOnly = true)
    public boolean canUserReview(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        return orderDetailRepository.hasUserPurchasedAndDelivered(userId, productId);
    }

    /**
     * FR21 & BR06 & ERR-12: Gửi đánh giá sản phẩm
     */
    @Transactional
    public Review addReview(Long userId, Long productId, ReviewDto dto) {
        if (!canUserReview(userId, productId)) {
            // ERR-12
            throw new IllegalArgumentException(
                    "Bạn chỉ có thể đánh giá sản phẩm sau khi đã mua và nhận hàng thành công!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại!"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại!"));

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(dto.getRating())
                .comment(dto.getComment().trim())
                .isVisible(true)
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Khách hàng {} gửi đánh giá {} sao cho sản phẩm {}", userId, dto.getRating(), productId);
        return saved;
    }

    /**
     * FR22: Admin lấy danh sách đánh giá
     */
    @Transactional(readOnly = true)
    public List<Review> getReviewsForAdmin(Long productId, Boolean isVisible) {
        if (productId != null) {
            return reviewRepository.findByProductProductIdOrderByCreatedAtDesc(productId);
        }
        if (isVisible != null) {
            return reviewRepository.findByIsVisibleOrderByCreatedAtDesc(isVisible);
        }
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * FR22: Ẩn/Hiện đánh giá
     */
    @Transactional
    public void toggleVisibility(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá!"));
        review.setIsVisible(!review.getIsVisible());
        reviewRepository.save(review);
        log.info("Admin đổi hiển thị đánh giá id={}: isVisible={}", reviewId, review.getIsVisible());
    }

    /**
     * FR22: Xóa đánh giá spam
     */
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá!"));
        reviewRepository.delete(review);
        log.info("Admin xóa đánh giá id={}", reviewId);
    }
}
