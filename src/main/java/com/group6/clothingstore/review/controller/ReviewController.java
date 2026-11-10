package com.group6.clothingstore.review.controller;

import com.group6.clothingstore.review.dto.ReviewDto;
import com.group6.clothingstore.review.service.ReviewService;
import com.group6.clothingstore.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    /**
     * FR21: Khách hàng gửi đánh giá chất lượng sản phẩm
     */
    @PostMapping("/shop/products/{id}/reviews")
    public String submitReview(@PathVariable("id") Long productId,
                               @Valid @ModelAttribute("reviewDto") ReviewDto dto,
                               BindingResult bindingResult,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login?required";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/shop/products/" + productId;
        }

        Long userId = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản!"))
                .getUserId();

        try {
            reviewService.addReview(userId, productId, dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cảm ơn bạn đã gửi đánh giá về sản phẩm!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/shop/products/" + productId;
    }
}
