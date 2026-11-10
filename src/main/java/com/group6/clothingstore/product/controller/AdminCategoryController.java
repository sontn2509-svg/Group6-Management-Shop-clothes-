package com.group6.clothingstore.product.controller;

import com.group6.clothingstore.product.dto.CategoryFormDto;
import com.group6.clothingstore.product.service.CategoryService;
import com.group6.clothingstore.shared.enums.CategoryStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        if (!model.containsAttribute("categoryFormDto")) {
            model.addAttribute("categoryFormDto", new CategoryFormDto());
        }
        model.addAttribute("categoryStatuses", CategoryStatus.values());
        return "admin/categories";
    }

    @PostMapping("/add")
    public String addCategory(@Valid @ModelAttribute("categoryFormDto") CategoryFormDto dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.categoryFormDto", bindingResult);
            redirectAttributes.addFlashAttribute("categoryFormDto", dto);
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu nhập không hợp lệ!");
            return "redirect:/admin/categories";
        }

        try {
            categoryService.createCategory(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục mới thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("categoryFormDto", dto);
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/edit/{id}")
    public String editCategory(@PathVariable("id") Integer id,
                               @Valid @ModelAttribute("categoryFormDto") CategoryFormDto dto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu cập nhật không hợp lệ!");
            return "redirect:/admin/categories";
        }

        try {
            categoryService.updateCategory(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable("id") Integer id,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục thành công!");
        } catch (IllegalArgumentException ex) {
            // BR05 & ERR-11: "Không thể xóa danh mục đang chứa sản phẩm. Hãy chuyển sản phẩm sang danh mục khác trước!"
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
