package com.group6.clothingstore.user.controller;

import com.group6.clothingstore.user.entity.User;
import com.group6.clothingstore.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserService userService;

    /**
     * FR05 / SCR-13: Danh sách khách hàng kèm tìm kiếm và phân trang
     */
    @GetMapping
    public String listUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<User> userPage = userService.getUsers(
                keyword,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        model.addAttribute("userPage", userPage);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("keyword", keyword != null ? keyword : "");

        return "admin/users";
    }

    /**
     * FR05 & BR08: Khóa hoặc Mở khóa tài khoản
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(
            @PathVariable("id") Long id,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.toggleUserStatus(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Thay đổi trạng thái tài khoản thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/users";
    }
}