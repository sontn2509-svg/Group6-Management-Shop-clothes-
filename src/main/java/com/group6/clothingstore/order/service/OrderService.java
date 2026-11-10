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

    /**
     * Hủy đơn hàng — cho phép hủy đến khi đơn chuyển sang SHIPPING.
     * Đơn COD chưa ship → hủy ngay. Đơn VNPAY đã trả (CONFIRMED) → dùng requestRefund.
     * Hủy thành công → cộng trả số lượng vào kho.
     */
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = getMyOrderDetail(orderId, userId);

        // Shopee-style: chặn hủy khi đã giao cho đơn vị vận chuyển
        if (order.getOrderStatus() == OrderStatus.SHIPPING
                || order.getOrderStatus() == OrderStatus.DELIVERED
                || order.getOrderStatus() == OrderStatus.CANCELLED
                || order.getOrderStatus() == OrderStatus.REFUNDED) {
            throw new IllegalArgumentException(
                    "Đơn hàng đã được bàn giao cho đơn vị vận chuyển hoặc đã kết thúc, không thể hủy!");
        }

        // Đơn VNPAY đã thanh toán (CONFIRMED) → không hủy trực tiếp, phải gửi yêu cầu hoàn tiền
        if (order.getPaymentMethod() == com.group6.clothingstore.shared.enums.PaymentMethod.BANK_TRANSFER
                && order.getOrderStatus() == OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException(
                    "Đơn hàng đã thanh toán qua VNPAY. Vui lòng gửi 'Yêu cầu hủy/hoàn tiền' để shop xử lý.");
        }

        restoreStock(order);
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Khách hủy đơn: orderId={}, userId={}", orderId, userId);
    }

    /**
     * Khách gửi yêu cầu hủy/hoàn tiền cho đơn VNPAY đã thanh toán (CONFIRMED) —
     * đơn chưa ship. Đơn chuyển sang REFUND_REQUESTED chờ admin duyệt.
     */
    @Transactional
    public void requestRefund(Long orderId, Long userId) {
        Order order = getMyOrderDetail(orderId, userId);

        if (order.getPaymentMethod() != com.group6.clothingstore.shared.enums.PaymentMethod.BANK_TRANSFER) {
            throw new IllegalArgumentException("Chỉ đơn thanh toán VNPAY mới cần yêu cầu hoàn tiền!");
        }
        if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException(
                    "Chỉ đơn đã thanh toán và chưa giao mới được yêu cầu hoàn tiền!");
        }

        order.setOrderStatus(OrderStatus.REFUND_REQUESTED);
        orderRepository.save(order);
        log.info("Khách yêu cầu hoàn tiền đơn: orderId={}, userId={}", orderId, userId);
    }

    /**
     * Admin duyệt yêu cầu hoàn tiền → REFUNDED + cộng trả kho.
     * Admin từ chối → đơn quay lại CONFIRMED.
     */
    @Transactional
    public void resolveRefund(Long orderId, boolean approve) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại!"));

        if (order.getOrderStatus() != OrderStatus.REFUND_REQUESTED) {
            throw new IllegalArgumentException("Đơn hàng không ở trạng thái chờ hoàn tiền!");
        }

        if (approve) {
            restoreStock(order);
            order.setOrderStatus(OrderStatus.REFUNDED);
            log.info("Admin duyệt hoàn tiền đơn {}", orderId);
        } else {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            log.info("Admin từ chối hoàn tiền đơn {} → quay lại CONFIRMED", orderId);
        }
        orderRepository.save(order);
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

    /** Danh sách đơn có phân trang cho Admin */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Order> getOrdersForAdmin(
            OrderStatus status, String keyword,
            org.springframework.data.domain.Pageable pageable) {
        // Khi có keyword thì lọc trên danh sách đầy đủ rồi phân trang thủ công
        if (keyword != null && !keyword.isBlank()) {
            List<Order> filtered = getOrdersForAdmin(status, keyword);
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            List<Order> content = start > filtered.size()
                    ? java.util.Collections.emptyList()
                    : filtered.subList(start, end);
            return new org.springframework.data.domain.PageImpl<>(content, pageable, filtered.size());
        }
        return (status == null)
                ? orderRepository.findAllByOrderByOrderDateDesc(pageable)
                : orderRepository.findByOrderStatusOrderByOrderDateDesc(status, pageable);
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