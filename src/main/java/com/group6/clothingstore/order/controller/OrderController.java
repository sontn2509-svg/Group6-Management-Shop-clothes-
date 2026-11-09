package com.group6.clothingstore.order.controller;

import com.group6.clothingstore.cart.entity.CartItem;
import com.group6.clothingstore.cart.service.CartService;
import com.group6.clothingstore.order.dto.CheckoutDto;
import com.group6.clothingstore.order.service.OrderService;
import com.group6.clothingstore.shared.enums.PaymentMethod;
import com.group6.clothingstore.user.entity.User;
import com.group6.clothingstore.user.repository.UserRepository;
import com.group6.clothingstore.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;
    private final UserRepository userRepository;

    private Long currentUserId(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản!"))
                .getUserId();
    }

    /** Hiển thị form thanh toán */
    @GetMapping("/checkout")
    public String checkoutPage(Model model, Principal principal) {
        Long userId = currentUserId(principal);
        User user = userService.getProfileByEmail(principal.getName());

        if (!model.containsAttribute("checkoutDto")) {
            model.addAttribute("checkoutDto", CheckoutDto.builder()
                    .receiverName(user.getFullName())
                    .receiverPhone(user.getPhone())
                    .shippingAddress(user.getAddress())
                    .paymentMethod(PaymentMethod.COD)
                    .build());
        }

        addCartToModel(model, userId);
        return "checkout/checkout";
    }

    /** Xác nhận đặt hàng */
    @PostMapping("/checkout")
    public String doCheckout(@Valid @ModelAttribute("checkoutDto") CheckoutDto checkoutDto,
                             BindingResult bindingResult,
                             Principal principal,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(principal);

        if (bindingResult.hasErrors()) {
            addCartToModel(model, userId);
            return "checkout/checkout";
        }

        try {
            orderService.checkout(userId, checkoutDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt hàng thành công! Đơn hàng đang chờ Admin duyệt.");
            return "redirect:/my-orders";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            addCartToModel(model, userId);
            return "checkout/checkout";
        }
    }

    /** Danh sách đơn hàng của tôi */
    @GetMapping("/my-orders")
    public String myOrders(Model model, Principal principal) {
        model.addAttribute("orders", orderService.getMyOrders(currentUserId(principal)));
        return "order/my-orders";
    }

    /** Chi tiết đơn hàng của tôi */
    @GetMapping("/my-orders/{id}")
    public String myOrderDetail(@PathVariable("id") Long id,
                                Model model,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("order", orderService.getMyOrderDetail(id, currentUserId(principal)));
            return "order/order-detail";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/my-orders";
        }
    }

    /** Hủy đơn hàng */
    @PostMapping("/my-orders/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Long id,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id, currentUserId(principal));
            redirectAttributes.addFlashAttribute("successMessage",
                    "Hủy đơn hàng thành công! Số lượng tồn kho đã được cộng trả lại.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/my-orders";
    }

    private void addCartToModel(Model model, Long userId) {
        List<CartItem> items = cartService.getCart(userId);
        model.addAttribute("cartItems", items);
        model.addAttribute("cartTotal", cartService.calculateTotal(items));
    }
}