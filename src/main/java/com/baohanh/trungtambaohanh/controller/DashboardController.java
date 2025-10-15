package com.baohanh.trungtambaohanh.controller	;

import com.baohanh.trungtambaohanh.repository.PhieuSuaChuaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/receptionist/dashboard")
public class DashboardController {

    @Autowired
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    @GetMapping
    public String showDashboard(Model model) {
        // Đếm số lượng phiếu theo các trạng thái quan trọng
        long phieuChoSua = phieuSuaChuaRepository.countByTrangThai("Mới tiếp nhận") + 
                             phieuSuaChuaRepository.countByTrangThai("Chờ sửa chữa");
        long phieuDangSua = phieuSuaChuaRepository.countByTrangThai("Đang sửa chữa");
        long phieuDaSuaXong = phieuSuaChuaRepository.countByTrangThai("Đã sửa xong");

        // Gửi các số liệu này ra ngoài view
        model.addAttribute("phieuChoSua", phieuChoSua);
        model.addAttribute("phieuDangSua", phieuDangSua);
        model.addAttribute("phieuDaSuaXong", phieuDaSuaXong);

        // Các thuộc tính cần thiết cho layout
        model.addAttribute("pageTitle", "Tổng quan");
        model.addAttribute("content", "~{receptionist/dashboard :: content}");
        
        // Trả về file layout chung
        return "receptionist/receptionist-layout";
    }
}