package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.dto.DashboardStatsDto;
import com.baohanh.trungtambaohanh.entity.LoaiThietBi;
import com.baohanh.trungtambaohanh.repository.LinhKienRepository;
import com.baohanh.trungtambaohanh.repository.LoaiThietBiRepository;
import com.baohanh.trungtambaohanh.repository.NhanVienRepository;
import com.baohanh.trungtambaohanh.repository.PhieuSuaChuaRepository;
import com.baohanh.trungtambaohanh.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    private final DashboardService dashboardService;
    private final LoaiThietBiRepository loaiThietBiRepository;
    private final LinhKienRepository linhKienRepository;
    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final NhanVienRepository nhanVienRepository;

    @Autowired
    public ManagerController(DashboardService dashboardService,
                             LoaiThietBiRepository loaiThietBiRepository,
                             LinhKienRepository linhKienRepository,
                             PhieuSuaChuaRepository phieuSuaChuaRepository,
                             NhanVienRepository nhanVienRepository) {
        this.dashboardService = dashboardService;
        this.loaiThietBiRepository = loaiThietBiRepository;
        this.linhKienRepository = linhKienRepository;
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.nhanVienRepository = nhanVienRepository;
    }

    // Trang chủ của Manager, có thể redirect đến dashboard
    @GetMapping
    public String showManagerHome() {
        return "redirect:/manager/dashboard";
    }

    // Trang Dashboard thống kê
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);
        return "manager/dashboard";
    }

    // --- QUẢN LÝ DANH MỤC LOẠI THIẾT BỊ ---
    @GetMapping("/devices")
    public String showDeviceTypesPage(Model model) {
        model.addAttribute("loaiThietBiList", loaiThietBiRepository.findAll());
        if (!model.containsAttribute("loaiThietBi")) {
            model.addAttribute("loaiThietBi", new LoaiThietBi());
        }
        return "manager/devices";
    }

    @PostMapping("/devices/save")
    public String saveDeviceType(@ModelAttribute("loaiThietBi") LoaiThietBi loaiThietBi, RedirectAttributes redirectAttributes) {
        try {
            String successMessage = (loaiThietBi.getMaLoaiTB() == null) ? "Thêm mới loại thiết bị thành công!" : "Cập nhật loại thiết bị thành công!";
            loaiThietBiRepository.save(loaiThietBi);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi lưu.");
        }
        return "redirect:/manager/devices";
    }

    @GetMapping("/devices/delete/{id}")
    public String deleteDeviceType(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            loaiThietBiRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa loại thiết bị thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa. Loại thiết bị này có thể đang được sử dụng.");
        }
        return "redirect:/manager/devices";
    }

    // --- CÁC TRANG CHƯA PHÁT TRIỂN (PLACEHOLDER) ---

    @GetMapping("/tickets")
    public String showTicketsPage(Model model) {
        // Tạm thời trả về danh sách rỗng để trang không lỗi
        model.addAttribute("phieuSuaChuaList", Collections.emptyList());
        return "manager/tickets"; // Cần tạo file /templates/manager/tickets.html
    }

    @GetMapping("/parts")
    public String showPartsPage(Model model) {
        // Tạm thời trả về danh sách rỗng
        model.addAttribute("linhKienList", Collections.emptyList());
        return "manager/parts"; // Cần tạo file /templates/manager/parts.html
    }

    @GetMapping("/employees")
    public String showEmployeesPage(Model model) {
        // Tạm thời trả về danh sách rỗng
        model.addAttribute("nhanVienList", Collections.emptyList());
        return "manager/employees"; // Cần tạo file /templates/manager/employees.html
    }
}