package com.group6.clothingstore.review.controller;

import com.group6.clothingstore.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
@Slf4j
public class AdminReviewController {

    private final ReviewService reviewService;

    /**
     * FR22: Admin xem toàn bộ đánh giá của khách
     */
    @GetMapping
    public String listReviews(@RequestParam(value = "productId", required = false) Long productId,
                              @RequestParam(value = "isVisible", required = false) Boolean isVisible,
                              Model model) {
        model.addAttribute("reviews", reviewService.getReviewsForAdmin(productId, isVisible));
        model.addAttribute("selectedProductId", productId);
        model.addAttribute("selectedIsVisible", isVisible);
        return "admin/reviews";
    }

    /**
     * FR22: Admin ẩn/hiện đánh giá
     */
    @PostMapping("/{id}/toggle-visibility")
    public String toggleVisibility(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.toggleVisibility(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái hiển thị đánh giá!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    /**
     * FR22: Admin xóa đánh giá spam/tiêu cực
     */
    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đánh giá thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/reviews";
    }
}
