package com.group6.clothingstore.cart.controller;

import com.group6.clothingstore.cart.entity.CartItem;
import com.group6.clothingstore.cart.service.CartService;
import com.group6.clothingstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    private Long currentUserId(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản!"))
                .getUserId();
    }


    @GetMapping("/cart")
    public String viewCart(Model model, Principal principal) {
        Long userId = currentUserId(principal);
        List<CartItem> items = cartService.getCart(userId);
        model.addAttribute("cartItems", items);
        model.addAttribute("cartTotal", cartService.calculateTotal(items));
        return "cart/cart";
    }


    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam("selectedSize") String selectedSize,
                            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(currentUserId(principal), productId, selectedSize, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }


    @PostMapping("/cart/update")
    public String updateItem(@RequestParam("cartItemId") Long cartItemId,
                             @RequestParam("selectedSize") String selectedSize,
                             @RequestParam("quantity") Integer quantity,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            cartService.updateItem(currentUserId(principal), cartItemId, selectedSize, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật giỏ hàng thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }


    @PostMapping("/cart/delete")
    public String removeItem(@RequestParam("cartItemId") Long cartItemId,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            cartService.removeItem(currentUserId(principal), cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa món hàng khỏi giỏ!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }
}