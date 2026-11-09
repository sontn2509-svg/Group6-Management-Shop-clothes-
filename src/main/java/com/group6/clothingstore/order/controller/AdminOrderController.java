package com.group6.clothingstore.order.controller;

import com.group6.clothingstore.order.service.OrderService;
import com.group6.clothingstore.shared.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController {

    private final OrderService orderService;

    /** SCR-12 / FR20: Danh sách đơn toàn shop + lọc */
    @GetMapping
    public String listOrders(@RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             Model model) {
        OrderStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = OrderStatus.valueOf(status);
            } catch (IllegalArgumentException ignored) {
            }
        }

        model.addAttribute("orders", orderService.getOrdersForAdmin(statusEnum, keyword));
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("statusFilter", status != null ? status : "");
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "admin/orders";
    }

    /** FR20: Cập nhật tiến độ giao hàng */
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") OrderStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/orders";
    }
}