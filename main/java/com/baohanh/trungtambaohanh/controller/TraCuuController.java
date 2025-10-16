package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import com.baohanh.trungtambaohanh.repository.PhieuSuaChuaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/receptionist/tra-cuu")
public class TraCuuController {

    @Autowired
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    // Hiển thị trang tìm kiếm và xử lý tìm kiếm
    @GetMapping
    public String showSearchPage(@RequestParam(name = "query", required = false) String query, Model model) {
        List<PhieuSuaChua> results = Collections.emptyList();
        
        if (query != null && !query.trim().isEmpty()) {
            // Sử dụng phương thức searchAndFilter có sẵn của bạn
            results = phieuSuaChuaRepository.searchAndFilter(query.trim(), null); // status = null để tìm tất cả
        }
        
        model.addAttribute("results", results);
        model.addAttribute("query", query);
        model.addAttribute("pageTitle", "Tra cứu sửa chữa");
        model.addAttribute("content", "~{receptionist/tra-cuu-form :: content}");
        return "receptionist/receptionist-layout";
    }

    // Hiển thị trang chi tiết của một phiếu sửa chữa
    @GetMapping("/{id}")
    public String showDetailPage(@PathVariable("id") Integer id, Model model) {
        Optional<PhieuSuaChua> phieuOptional = phieuSuaChuaRepository.findById(id);

        if (phieuOptional.isEmpty()) {
            return "redirect:/receptionist/tra-cuu?error=notfound";
        }

        model.addAttribute("phieu", phieuOptional.get());
        model.addAttribute("pageTitle", "Chi tiết Phiếu #" + id);
        model.addAttribute("content", "~{receptionist/chi-tiet-phieu :: content}");
        return "receptionist/receptionist-layout";
    }
}