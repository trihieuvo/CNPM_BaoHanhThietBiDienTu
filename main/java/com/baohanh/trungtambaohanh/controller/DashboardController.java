package com.baohanh.trungtambaohanh.controller; // Package của bạn là đúng rồi

import com.baohanh.trungtambaohanh.repository.PhieuSuaChuaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/receptionist/dashboard") // <--- SỬA DÒNG NÀY
public class DashboardController {

    @Autowired
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    @GetMapping
    public String showDashboard(Model model) {
        long phieuChoSua = phieuSuaChuaRepository.countByTrangThai("Mới tiếp nhận") + 
                             phieuSuaChuaRepository.countByTrangThai("Chờ sửa chữa");
        long phieuDangSua = phieuSuaChuaRepository.countByTrangThai("Đang sửa chữa");
        long phieuDaSuaXong = phieuSuaChuaRepository.countByTrangThai("Đã sửa xong");

        model.addAttribute("phieuChoSua", phieuChoSua);
        model.addAttribute("phieuDangSua", phieuDangSua);
        model.addAttribute("phieuDaSuaXong", phieuDaSuaXong);

        model.addAttribute("pageTitle", "Tổng quan");
        model.addAttribute("content", "~{receptionist/dashboard :: content}");
        
        return "receptionist/receptionist-layout";
    }
}