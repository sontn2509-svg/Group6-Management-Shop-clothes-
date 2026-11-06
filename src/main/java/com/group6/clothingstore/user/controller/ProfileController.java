package com.group6.clothingstore.user.controller;

import com.group6.clothingstore.user.dto.ChangePasswordDto;
import com.group6.clothingstore.user.dto.ProfileUpdateDto;
import com.group6.clothingstore.user.entity.User;
import com.group6.clothingstore.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserService userService;

    /**
     * FR03 / SCR-08: Xem thông tin cá nhân
     */
    @GetMapping
    public String viewProfile(Model model, Principal principal) {
        User user = userService.getProfileByEmail(principal.getName());

        if (!model.containsAttribute("profileDto")) {
            ProfileUpdateDto profileDto = ProfileUpdateDto.builder()
                    .fullName(user.getFullName())
                    .phone(user.getPhone() != null ? user.getPhone() : "")
                    .address(user.getAddress() != null ? user.getAddress() : "")
                    .build();
            model.addAttribute("profileDto", profileDto);
        }

        if (!model.containsAttribute("changePasswordDto")) {
            model.addAttribute("changePasswordDto", new ChangePasswordDto());
        }

        model.addAttribute("user", user);
        return "customer/profile";
    }

    /**
     * FR04: Cập nhật thông tin liên hệ và địa chỉ
     */
    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("profileDto") ProfileUpdateDto profileDto,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            User user = userService.getProfileByEmail(principal.getName());
            model.addAttribute("user", user);
            model.addAttribute("changePasswordDto", new ChangePasswordDto());
            return "customer/profile";
        }

        try {
            userService.updateProfile(principal.getName(), profileDto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/profile";
    }

    /**
     * FR04: Đổi mật khẩu
     */
    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute("changePasswordDto") ChangePasswordDto changePasswordDto,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            User user = userService.getProfileByEmail(principal.getName());
            model.addAttribute("user", user);
            model.addAttribute("profileDto", ProfileUpdateDto.builder()
                    .fullName(user.getFullName())
                    .phone(user.getPhone())
                    .address(user.getAddress())
                    .build());
            return "customer/profile";
        }

        try {
            userService.changePassword(principal.getName(), changePasswordDto);
            redirectAttributes.addFlashAttribute("passwordSuccessMessage", "Đổi mật khẩu thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("passwordErrorMessage", ex.getMessage());
        }

        return "redirect:/profile";
    }
}