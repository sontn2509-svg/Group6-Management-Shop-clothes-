package com.group6.clothingstore.product.controller;

import com.group6.clothingstore.product.entity.Product;
import com.group6.clothingstore.product.service.ShopService;
import com.group6.clothingstore.review.dto.ReviewDto;
import com.group6.clothingstore.review.service.ReviewService;
import com.group6.clothingstore.user.entity.User;
import com.group6.clothingstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shop;
    private final ReviewService reviewService;
    private final UserRepository userRepository;

    // FR11: Homepage uses existing AuthController root mapping, do not create another '/'.
    @GetMapping("/shop")
    public String listing(@RequestParam(defaultValue = "") String keyword,
                          @RequestParam(required = false) Integer categoryId,
                          @RequestParam(defaultValue = "") String gender,
                          @RequestParam(defaultValue = "") String size,
                          @RequestParam(required = false) BigDecimal minPrice,
                          @RequestParam(required = false) BigDecimal maxPrice,
                          @RequestParam(defaultValue = "0") int page, Model model) {
        if (page < 0 || minPrice != null && minPrice.signum() < 0 ||
                maxPrice != null && maxPrice.signum() < 0 ||
                minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bộ lọc không hợp lệ");
        }
        Page<?> result;
        try { result = shop.search(keyword, categoryId, gender, size, minPrice, maxPrice, page); }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()); }
        model.addAttribute("products", result);
        model.addAttribute("categories", shop.categories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("gender", gender);
        model.addAttribute("size", size);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        return "shop/products";
    }

    @GetMapping("/shop/products/{id}")
    public String detail(@PathVariable long id, Model model, Principal principal) {
        Product product;
        try {
            product = shop.detail(id);
            model.addAttribute("product", product);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        List<String> sizeList = (product.getAvailableSizes() != null)
                ? Arrays.asList(product.getAvailableSizes().split("\\s*,\\s*"))
                : List.of("S", "M", "L", "XL");
        model.addAttribute("availableSizesList", sizeList);

        // Reviews (FR21)
        model.addAttribute("reviews", reviewService.getVisibleReviews(id));
        model.addAttribute("averageRating", reviewService.getAverageRating(id));
        model.addAttribute("reviewCount", reviewService.countReviews(id));
        model.addAttribute("reviewDto", new ReviewDto());

        // BR06: Kiểm tra người dùng có quyền đánh giá không
        boolean canReview = false;
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                canReview = reviewService.canUserReview(user.getUserId(), id);
            }
        }
        model.addAttribute("canReview", canReview);

        return "shop/product-detail";
    }
}
