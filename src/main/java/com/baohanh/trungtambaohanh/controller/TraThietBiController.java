package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import com.baohanh.trungtambaohanh.repository.PhieuSuaChuaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/receptionist/tra-thiet-bi")
public class TraThietBiController {

    @Autowired
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    // Hiển thị danh sách các phiếu "Đã sửa xong"
    @GetMapping
    public String showListPage(Model model) {
        // Lấy tất cả các phiếu có trạng thái là "Đã sửa xong"
        List<PhieuSuaChua> phieuCanTra = phieuSuaChuaRepository.findByTrangThai("Đã sửa xong");

        model.addAttribute("phieuList", phieuCanTra);
        model.addAttribute("pageTitle", "Trả thiết bị & Thanh toán");
        model.addAttribute("content", "~{receptionist/tra-thiet-bi-list :: content}");
        return "receptionist/receptionist-layout";
    }

    // Xử lý xác nhận thanh toán và trả thiết bị
    @PostMapping("/{id}/thanh-toan")
    public String processPayment(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        PhieuSuaChua phieuSuaChua = phieuSuaChuaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mã phiếu không hợp lệ: " + id));

        // Cập nhật trạng thái phiếu
        phieuSuaChua.setTrangThai("Đã trả khách");
        phieuSuaChuaRepository.save(phieuSuaChua);

        redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận trả thiết bị cho phiếu #" + id + " thành công!");
        return "redirect:/receptionist/tra-thiet-bi";
    }
}