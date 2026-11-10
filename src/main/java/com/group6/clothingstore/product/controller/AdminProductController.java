package com.group6.clothingstore.product.controller;

import com.group6.clothingstore.product.dto.ProductFormDto;
import com.group6.clothingstore.product.entity.Product;
import com.group6.clothingstore.product.service.CategoryService;
import com.group6.clothingstore.product.service.ProductManageService;
import com.group6.clothingstore.shared.enums.ProductGender;
import com.group6.clothingstore.shared.enums.ProductStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {

    private final ProductManageService productManageService;
    private final CategoryService categoryService;

    /**
     * FR06: Xem kho quần áo (Admin)
     */
    @GetMapping
    public String listProducts(@RequestParam(value = "categoryId", required = false) Integer categoryId,
                               @RequestParam(value = "status", required = false) ProductStatus status,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               Model model) {
        model.addAttribute("products", productManageService.getAllProducts(categoryId, status, keyword));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("productStatuses", ProductStatus.values());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "admin/products";
    }

    /**
     * FR07: Hiển thị form thêm mẫu quần áo mới
     */
    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        if (!model.containsAttribute("productFormDto")) {
            ProductFormDto dto = new ProductFormDto();
            dto.setSizes(List.of("S", "M", "L", "XL"));
            dto.setGender(ProductGender.UNISEX);
            dto.setStatus(ProductStatus.ACTIVE);
            model.addAttribute("productFormDto", dto);
        }
        populateFormData(model);
        return "admin/product-form";
    }

    /**
     * FR07: Xử lý thêm mẫu quần áo mới
     */
    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute("productFormDto") ProductFormDto dto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateFormData(model);
            return "admin/product-form";
        }

        try {
            productManageService.createProduct(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm mẫu quần áo mới thành công!");
            return "redirect:/admin/products";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateFormData(model);
            return "admin/product-form";
        }
    }

    /**
     * FR08: Hiển thị form cập nhật thông tin quần áo
     */
    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productManageService.getProductById(id);
            List<String> sizes = (product.getAvailableSizes() != null)
                    ? Arrays.asList(product.getAvailableSizes().split("\\s*,\\s*"))
                    : List.of();

            ProductFormDto dto = ProductFormDto.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .categoryId(product.getCategory().getCategoryId())
                    .price(product.getPrice())
                    .stockQuantity(product.getStockQuantity())
                    .sizes(sizes)
                    .color(product.getColor())
                    .material(product.getMaterial())
                    .gender(product.getGender())
                    .description(product.getDescription())
                    .imageUrl(product.getImageUrl())
                    .status(product.getStatus())
                    .build();

            model.addAttribute("productFormDto", dto);
            populateFormData(model);
            model.addAttribute("isEdit", true);
            return "admin/product-form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/products";
        }
    }

    /**
     * FR08: Xử lý cập nhật quần áo
     */
    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("productFormDto") ProductFormDto dto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateFormData(model);
            model.addAttribute("isEdit", true);
            return "admin/product-form";
        }

        try {
            productManageService.updateProduct(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
            return "redirect:/admin/products";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateFormData(model);
            model.addAttribute("isEdit", true);
            return "admin/product-form";
        }
    }

    /**
     * FR09: Ẩn / Ngừng kinh doanh mẫu
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleProductStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productManageService.toggleProductStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái sản phẩm!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/products";
    }

    /**
     * BR05: Xóa sản phẩm (nếu đã có đơn thì chỉ chuyển sang INACTIVE)
     */
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean hardDeleted = productManageService.deleteOrDeactivateProduct(id);
            if (hardDeleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã xóa hoàn toàn sản phẩm khỏi hệ thống!");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Sản phẩm đã từng phát sinh trong đơn hàng, hệ thống đã chuyển trạng thái sang NGỪNG KINH DOANH (INACTIVE) để bảo toàn dữ liệu lịch sử!");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/products";
    }

    private void populateFormData(Model model) {
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("allSizes", List.of("S", "M", "L", "XL"));
        model.addAttribute("genders", ProductGender.values());
        model.addAttribute("statuses", ProductStatus.values());
    }
}
