package com.group6.clothingstore.report.controller;

import com.group6.clothingstore.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardController {

    private final ReportService reportService;

    /**
     * SCR-09 & FR23, FR24, FR25: Admin Dashboard & Thống kê
     */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("kpi", reportService.getDashboardKpi());
        model.addAttribute("monthlyRevenues", reportService.getMonthlyRevenueReport());
        model.addAttribute("topProducts", reportService.getTopSellingProducts());
        return "admin/dashboard";
    }
}
