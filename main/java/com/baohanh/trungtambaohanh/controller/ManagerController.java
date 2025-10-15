package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.dto.DashboardStatsDto;
import com.baohanh.trungtambaohanh.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    private final DashboardService dashboardService;

    public ManagerController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    // Ánh xạ tới trang dashboard thống kê
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);
        return "manager/dashboard"; // Trả về file dashboard.html
    }
    
    // Ánh xạ tới trang menu chính của quản lý
    @GetMapping
    public String showManagerMenu() {
        return "manager/menu";
    }
}