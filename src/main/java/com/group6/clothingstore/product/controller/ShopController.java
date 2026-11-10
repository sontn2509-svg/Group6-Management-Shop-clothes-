package com.group6.clothingstore.product.controller;

import com.group6.clothingstore.product.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shop;

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
    public String detail(@PathVariable long id, Model model) {
        try { model.addAttribute("product", shop.detail(id)); }
        catch (NoSuchElementException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage()); }
        return "shop/product-detail";
    }
}
