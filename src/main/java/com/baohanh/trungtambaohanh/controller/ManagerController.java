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
    @GetMapping("/devices")
    public String showDevicesPage() {
        // Thêm logic để lấy danh sách thiết bị từ service và truyền vào model
        // model.addAttribute("devices", deviceService.findAll());
        return "manager/devices";
    }

    @GetMapping("/parts")
    public String showPartsPage() {
        // Thêm logic để lấy danh sách linh kiện
        return "manager/parts";
    }

    @GetMapping("/tickets")
    public String showTicketsPage() {
        // Thêm logic để lấy danh sách phiếu sửa
        return "manager/tickets";
    }

    @GetMapping("/employees")
    public String showEmployeesPage() {
        // Thêm logic để lấy danh sách nhân viên
        return "manager/employees";
    }
    @GetMapping("/revenue-report")
    public String showRevenueReportPage() {
        // TODO: Create revenue-report.html or redirect
        // For now, let's redirect to dashboard to avoid errors
        return "redirect:/manager/dashboard";
    }

    @GetMapping("/repair-stats")
    public String showRepairStatsPage() {
        // TODO: Create repair-stats.html or redirect
        return "redirect:/manager/dashboard";
    }

    @GetMapping("/settings")
    public String showSettingsPage() {
        return "manager/settings";
    }
}