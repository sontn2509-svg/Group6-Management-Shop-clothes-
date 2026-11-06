package com.group6.clothingstore.auth.controller;

import com.group6.clothingstore.auth.dto.LoginDto;
import com.group6.clothingstore.auth.dto.RegisterDto;
import com.group6.clothingstore.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    /**
     * SCR-02: Hiển thị trang đăng nhập
     */
    @GetMapping("/login")
    public String viewLoginPage(Model model) {
        if (isAuthenticated()) {
            return "redirect:/";
        }
        model.addAttribute("loginDto", new LoginDto());
        return "auth/login";
    }

    /**
     * Hiển thị trang đăng ký
     */
    @GetMapping("/register")
    public String viewRegisterPage(Model model) {
        if (isAuthenticated()) {
            return "redirect:/";
        }
        model.addAttribute("registerDto", new RegisterDto());
        return "auth/register";
    }

    /**
     * FR01: Xử lý đăng ký tài khoản khách hàng mới
     */
    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("registerDto") RegisterDto registerDto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(registerDto);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }
    }
}