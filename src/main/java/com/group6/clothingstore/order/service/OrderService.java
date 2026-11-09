package com.group6.clothingstore.order.service;

import com.group6.clothingstore.cart.entity.CartItem;
import com.group6.clothingstore.cart.repository.CartRepository;
import com.group6.clothingstore.order.dto.CheckoutDto;
import com.group6.clothingstore.order.entity.Order;
import com.group6.clothingstore.order.entity.OrderDetail;
import com.group6.clothingstore.order.repository.OrderRepository;
import com.group6.clothingstore.product.entity.Product;
import com.group6.clothingstore.product.repository.ProductRepository;
import com.group6.clothingstore.shared.enums.OrderStatus;
import com.group6.clothingstore.shared.enums.ProductStatus;
import com.group6.clothingstore.user.entity.User;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /** Đặt hàng: tạo đơn, trừ tồn kho và rollback toàn bộ nếu có lỗi */
    @Transactional
    public Order checkout(Long userId, CheckoutDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản!"));

        List<CartItem> cartItems = cartRepository.findByUserUserId(userId);

        // Giỏ hàng trống thì không cho đặt hàng
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "Giỏ hàng của bạn đang trống. Vui lòng chọn sản phẩm trước khi thanh toán!");
        }

        Order order = Order.builder()
                .user(user)
                .receiverName(dto.getReceiverName().trim())
                .receiverPhone(dto.getReceiverPhone().trim())
                .shippingAddress(dto.getShippingAddress().trim())
                .note(dto.getNote() != null ? dto.getNote().trim() : null)
                .paymentMethod(dto.getPaymentMethod())
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .orderDetails(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Kiểm tra lại tồn kho tại thời điểm xác nhận đặt hàng
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new IllegalArgumentException(
                        "Sản phẩm '" + product.getProductName() + "' hiện không còn kinh doanh!");
            }
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Sản phẩm '" + product.getProductName()
                        + "' vừa hết hàng hoặc không đủ số lượng. Vui lòng cập nhật lại giỏ hàng!");
            }

            // Trừ tồn kho; tự chuyển sang hết hàng khi về 0
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            if (product.getStockQuantity() == 0) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            }
            productRepository.save(product);

            // unitPrice là giá tại thời điểm đặt hàng, không đổi theo giá sản phẩm sau này
            order.getOrderDetails().add(OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .selectedSize(cartItem.getSelectedSize())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build());

            total = total.add(product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);

        // Xóa giỏ sau khi đặt thành công
        cartRepository.deleteByUserUserId(userId);

        log.info("Đặt hàng thành công: orderId={}, userId={}, total={}", savedOrder.getOrderId(), userId, total);
        return savedOrder;
    }

    /** Lịch sử đơn hàng của tôi */
    @Transactional(readOnly = true)
    public List<Order> getMyOrders(Long userId) {
        return orderRepository.findByUserUserIdOrderByOrderDateDesc(userId);
    }

    /** Chi tiết một đơn hàng (chỉ chủ đơn mới xem được) */
    @Transactional(readOnly = true)
    public Order getMyOrderDetail(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại!"));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xem đơn hàng này!");
        }
        return order;
    }

    /** Hủy đơn hàng — chỉ khi đang chờ duyệt, đồng thời cộng trả số lượng vào kho */
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = getMyOrderDetail(orderId, userId);

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Đơn hàng đã được bàn giao cho đơn vị vận chuyển, không thể thực hiện hủy!");
        }

        restoreStock(order);
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Khách hủy đơn: orderId={}, userId={}", orderId, userId);
    }

    /** Admin duyệt và cập nhật tiến độ giao hàng */
    @Transactional
    public void updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại!"));

        if (order.getOrderStatus() == newStatus) {
            return;
        }

        // Admin hủy đơn cũng phải cộng trả lại số lượng vào kho
        if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setOrderStatus(newStatus);
        orderRepository.save(order);
        log.info("Admin cập nhật đơn {} sang {}", orderId, newStatus);
    }

    /** Danh sách đơn toàn shop, lọc theo trạng thái và từ khóa */
    @Transactional(readOnly = true)
    public List<Order> getOrdersForAdmin(OrderStatus status, String keyword) {
        List<Order> orders = (status == null)
                ? orderRepository.findAllByOrderByOrderDateDesc()
                : orderRepository.findByOrderStatusOrderByOrderDateDesc(status);

        if (keyword == null || keyword.isBlank()) {
            return orders;
        }

        String kw = keyword.trim().toLowerCase();
        return orders.stream()
                .filter(o -> String.valueOf(o.getOrderId()).contains(kw)
                        || o.getReceiverName().toLowerCase().contains(kw)
                        || o.getReceiverPhone().contains(kw))
                .toList();
    }

    /** Cộng trả lại số lượng sản phẩm trong đơn vào kho */
    private void restoreStock(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = detail.getProduct();
            product.setStockQuantity(product.getStockQuantity() + detail.getQuantity());

            if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
                product.setStatus(ProductStatus.ACTIVE);
            }
            productRepository.save(product);
        }
    }
}