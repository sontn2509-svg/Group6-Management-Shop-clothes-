package com.group6.clothingstore.payment.controller;

import com.group6.clothingstore.order.entity.Order;
import com.group6.clothingstore.order.repository.OrderRepository;
import com.group6.clothingstore.order.service.OrderService;
import com.group6.clothingstore.payment.service.VnPayService;
import com.group6.clothingstore.shared.enums.OrderStatus;
import com.group6.clothingstore.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class VnPayController {

    private final VnPayService vnPayService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    private Long currentUserId(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản!"))
                .getUserId();
    }

    /**
     * Khởi tạo thanh toán VNPAY cho một đơn hàng đang chờ thanh toán
     */
    @GetMapping("/vnpay/{orderId}")
    public String payOrderWithVnPay(@PathVariable("orderId") Long orderId,
                                    Principal principal,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getMyOrderDetail(orderId, currentUserId(principal));
            if (order.getOrderStatus() != OrderStatus.PENDING) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Đơn hàng #" + orderId + " không còn ở trạng thái chờ thanh toán!");
                return "redirect:/my-orders/" + orderId;
            }
            String paymentUrl = vnPayService.createPaymentUrl(order, request);
            return "redirect:" + paymentUrl;
        } catch (Exception ex) {
            log.error("Lỗi tạo thanh toán VNPAY cho đơn #{}: {}", orderId, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/my-orders";
        }
    }

    /**
     * Return URL: VNPAY chuyển hướng trình duyệt của khách hàng về đây sau khi thanh toán xong
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String vnpTxnRef = request.getParameter("vnp_TxnRef");
        String vnpResponseCode = request.getParameter("vnp_ResponseCode");
        String vnpTransactionNo = request.getParameter("vnp_TransactionNo");

        if (!vnPayService.verifySignature(request)) {
            log.warn("Cảnh báo: Chữ ký VNPAY Return không hợp lệ! txnRef={}", vnpTxnRef);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Chữ ký bảo mật VNPAY không hợp lệ (Invalid Checksum)!");
            return "redirect:/my-orders";
        }

        try {
            Long orderId = vnPayService.extractOrderId(vnpTxnRef);
            if ("00".equals(vnpResponseCode)) {
                Order order = orderRepository.findById(orderId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId));
                if (order.getOrderStatus() == OrderStatus.PENDING) {
                    orderService.updateStatus(orderId, OrderStatus.CONFIRMED);
                }
                redirectAttributes.addFlashAttribute("successMessage",
                        "Thanh toán VNPAY thành công (Mã GD: " + vnpTransactionNo + ")! Đơn hàng #" + orderId + " đã được xác nhận.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Giao dịch VNPAY không thành công hoặc đã bị hủy (Mã phản hồi: " + vnpResponseCode
                                + "). Bạn có thể nhấn 'Thanh toán qua VNPAY' để thử lại.");
            }
            return "redirect:/my-orders/" + orderId;
        } catch (Exception ex) {
            log.error("Lỗi xử lý VNPAY Return: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xử lý kết quả thanh toán: " + ex.getMessage());
            return "redirect:/my-orders";
        }
    }

    /**
     * IPN URL (Server-to-Server): VNPAY gọi ngầm để cập nhật trạng thái đơn hàng
     */
    @GetMapping("/vnpay-ipn")
    @ResponseBody
    public Map<String, String> vnpayIpn(HttpServletRequest request) {
        try {
            if (!vnPayService.verifySignature(request)) {
                return Map.of("RspCode", "97", "Message", "Invalid Checksum");
            }

            String vnpTxnRef = request.getParameter("vnp_TxnRef");
            String vnpResponseCode = request.getParameter("vnp_ResponseCode");
            String vnpAmountStr = request.getParameter("vnp_Amount");

            Long orderId = vnPayService.extractOrderId(vnpTxnRef);
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return Map.of("RspCode", "01", "Message", "Order not Found");
            }

            Order order = orderOpt.get();
            long expectedAmount = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
            long vnpAmount = Long.parseLong(vnpAmountStr);
            if (expectedAmount != vnpAmount) {
                return Map.of("RspCode", "04", "Message", "Invalid Amount");
            }

            if (order.getOrderStatus() != OrderStatus.PENDING) {
                return Map.of("RspCode", "02", "Message", "Order already confirmed");
            }

            if ("00".equals(vnpResponseCode)) {
                orderService.updateStatus(orderId, OrderStatus.CONFIRMED);
            }
            return Map.of("RspCode", "00", "Message", "Confirm Success");
        } catch (Exception ex) {
            log.error("Lỗi xử lý VNPAY IPN", ex);
            return Map.of("RspCode", "99", "Message", "Unknow error");
        }
    }
}

