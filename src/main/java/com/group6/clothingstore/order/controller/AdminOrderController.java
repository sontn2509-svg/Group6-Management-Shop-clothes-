package com.group6.clothingstore.order.controller;

import com.group6.clothingstore.order.entity.Order;
import com.group6.clothingstore.order.service.OrderService;
import com.group6.clothingstore.shared.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    /** Danh sách đơn toàn shop, kèm lọc theo trạng thái, từ khóa và phân trang */
    @GetMapping
    public String listOrders(@RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             Model model) {
        OrderStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = OrderStatus.valueOf(status);
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<Order> orderPage = orderService.getOrdersForAdmin(statusEnum, keyword,
                PageRequest.of(page, size));

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("statusFilter", status != null ? status : "");
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "admin/orders";
    }

    /** Cập nhật tiến độ giao hàng của đơn */
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