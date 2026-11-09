package com.group6.clothingstore.cart.service;

import com.group6.clothingstore.cart.entity.CartItem;
import com.group6.clothingstore.cart.repository.CartRepository;
import com.group6.clothingstore.product.entity.Product;
import com.group6.clothingstore.product.repository.ProductRepository;
import com.group6.clothingstore.shared.enums.ProductStatus;
import com.group6.clothingstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CartItem> getCart(Long userId) {
        return cartRepository.findByUserUserId(userId);
    }

    public BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(i -> i.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Thêm sản phẩm vào giỏ, không vượt quá tồn kho và chỉ áp dụng cho hàng đang bán
     */
    @Transactional
    public void addToCart(Long userId, Long productId, String selectedSize, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại!"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException("Sản phẩm này hiện không còn kinh doanh!");
        }

        Optional<CartItem> existing = cartRepository
                .findByUserUserIdAndProductProductIdAndSelectedSize(userId, productId, selectedSize);

        int newQuantity = quantity + existing.map(CartItem::getQuantity).orElse(0);

        if (newQuantity > product.getStockQuantity()) {
            throw new IllegalArgumentException(
                    "Số lượng yêu cầu vượt quá số lượng hàng còn trong kho (Còn lại: "
                            + product.getStockQuantity() + ")!");
        }

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(newQuantity);
            cartRepository.save(item);
        } else {
            cartRepository.save(CartItem.builder()
                    .user(userRepository.getReferenceById(userId))
                    .product(product)
                    .selectedSize(selectedSize)
                    .quantity(newQuantity)
                    .build());
        }
    }

    /**
     * Đổi số lượng và đổi Size của món trong giỏ
     */
    @Transactional
    public void updateItem(Long userId, Long cartItemId, String selectedSize, Integer quantity) {
        CartItem item = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Món hàng không tồn tại!"));

        if (!item.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền sửa món hàng này!");
        }
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0!");
        }
        if (quantity > item.getProduct().getStockQuantity()) {
            throw new IllegalArgumentException(
                    "Số lượng yêu cầu vượt quá số lượng hàng còn trong kho (Còn lại: "
                            + item.getProduct().getStockQuantity() + ")!");
        }

        item.setSelectedSize(selectedSize);
        item.setQuantity(quantity);
        cartRepository.save(item);
    }

    /**
     * Xóa món hàng khỏi giỏ
     */
    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        CartItem item = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Món hàng không tồn tại!"));

        if (!item.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xóa món hàng này!");
        }
        cartRepository.delete(item);
    }

}