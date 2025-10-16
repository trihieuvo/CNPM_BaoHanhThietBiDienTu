package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.entity.KhachHang;
import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import com.baohanh.trungtambaohanh.entity.ThietBi;
import com.baohanh.trungtambaohanh.repository.KhachHangRepository;
import com.baohanh.trungtambaohanh.repository.PhieuSuaChuaRepository;
import com.baohanh.trungtambaohanh.repository.ThietBiRepository;
import jakarta.servlet.http.HttpServletRequest; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/receptionist") 
public class ReceptionistController {

    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final KhachHangRepository khachHangRepository;
    private final ThietBiRepository thietBiRepository;

    @Autowired
    public ReceptionistController(PhieuSuaChuaRepository phieuSuaChuaRepository,
                                  KhachHangRepository khachHangRepository,
                                  ThietBiRepository thietBiRepository) {
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.khachHangRepository = khachHangRepository;
        this.thietBiRepository = thietBiRepository;
    }

    // ===================================
    // 1. DASHBOARD - /receptionist/dashboard
    // ===================================
    
    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpServletRequest request) {
        long phieuChoSua = phieuSuaChuaRepository.countByTrangThai("Mới tiếp nhận") +
                             phieuSuaChuaRepository.countByTrangThai("Chờ sửa chữa");
        long phieuDangSua = phieuSuaChuaRepository.countByTrangThai("Đang sửa chữa");
        long phieuDaSuaXong = phieuSuaChuaRepository.countByTrangThai("Đã sửa xong");

        model.addAttribute("phieuChoSua", phieuChoSua);
        model.addAttribute("phieuDangSua", phieuDangSua);
        model.addAttribute("phieuDaSuaXong", phieuDaSuaXong);
        model.addAttribute("pageTitle", "Tổng quan");
        model.addAttribute("currentPath", request.getServletPath()); 
        
        // Trả về tên file HTML trực tiếp
        return "receptionist/dashboard"; 
    }

    // ===================================
    // 2. TIẾP NHẬN BẢO HÀNH - /receptionist/tiep-nhan
    // ===================================

    @GetMapping("/tiep-nhan")
    public String showTiepNhanForm(Model model, HttpServletRequest request) {
        model.addAttribute("pageTitle", "Tiếp nhận bảo hành");
        model.addAttribute("phieuSuaChua", new PhieuSuaChua()); 
        model.addAttribute("thietBi", new ThietBi());
        model.addAttribute("currentPath", request.getServletPath());
           
        // Trả về tên file HTML trực tiếp
        return "receptionist/tiep-nhan-form";
    }

    @GetMapping("/tiep-nhan/api/customers/search")
    @ResponseBody
    public ResponseEntity<List<KhachHang>> searchCustomers(@RequestParam("sdt") String sdt) {
        return ResponseEntity.ok(khachHangRepository.findBySoDienThoaiContainingIgnoreCase(sdt));
    }

    @PostMapping("/tiep-nhan")
    public String processTiepNhanForm(@RequestParam(value = "khachHangId", required = false) String khachHangIdStr,
                                      @RequestParam("hoTen") String hoTen,
                                      @RequestParam("soDienThoai") String soDienThoai,
                                      @RequestParam(value = "email", required = false) String email,
                                      @RequestParam(value = "diaChi", required = false) String diaChi,
                                      @ModelAttribute ThietBi thietBi,
                                      @ModelAttribute PhieuSuaChua phieuSuaChua,
                                      RedirectAttributes redirectAttributes) { 
        
        int tempKhachHangId = 0; 
        if (khachHangIdStr != null && !khachHangIdStr.isEmpty() && !khachHangIdStr.equals("undefined")) {
            try {
                tempKhachHangId = Integer.parseInt(khachHangIdStr);
            } catch (NumberFormatException e) {
                // Xử lý lỗi
            }
        }
        
        final Integer khachHangIdFinal = tempKhachHangId;

        try {
            KhachHang khachHang;
            
            // 1. Tìm hoặc tạo Khách hàng
            if (khachHangIdFinal > 0) { 
                khachHang = khachHangRepository.findById(khachHangIdFinal)
                        .orElseThrow(() -> new IllegalArgumentException("Mã khách hàng không hợp lệ:" + khachHangIdFinal));
            } else {
                khachHang = khachHangRepository.findBySoDienThoai(soDienThoai)
                        .orElseGet(() -> {
                            KhachHang newCustomer = new KhachHang();
                            newCustomer.setHoTen(hoTen);
                            newCustomer.setSoDienThoai(soDienThoai);
                            newCustomer.setEmail(email);
                            newCustomer.setDiaChi(diaChi);
                            return khachHangRepository.save(newCustomer);
                        });
            }

            // 2. Lưu Thiết bị
            thietBi.setKhachHang(khachHang);
            thietBiRepository.save(thietBi); 

            // 3. Lưu Phiếu sửa chữa
            phieuSuaChua.setKhachHang(khachHang);
            phieuSuaChua.setThietBi(thietBi);
            phieuSuaChua.setNgayTiepNhan(OffsetDateTime.now());
            phieuSuaChua.setTrangThai("Mới tiếp nhận");
            phieuSuaChuaRepository.save(phieuSuaChua);

            redirectAttributes.addFlashAttribute("successMessage", 
                "Tiếp nhận phiếu sửa chữa **#" + phieuSuaChua.getMaPhieu() + "** thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lập phiếu: " + e.getMessage());
        }

        return "redirect:/receptionist/tiep-nhan";
    }

    // ===================================
    // 3. TRA CỨU SỬA CHỮA - /receptionist/tra-cuu
    // ===================================

    @GetMapping("/tra-cuu")
    public String showSearchPage(@RequestParam(name = "query", required = false) String query, 
                                 @RequestParam(name = "trangThai", required = false) String trangThai, 
                                 Model model,
                                 HttpServletRequest request) { 
        
        List<PhieuSuaChua> results = Collections.emptyList();
        
        // Logic tìm kiếm và lọc
        if ((query != null && !query.trim().isEmpty()) || 
            (trangThai != null && !trangThai.isEmpty())) { 
            
            results = phieuSuaChuaRepository.searchAndFilter(
                query != null ? query.trim() : null, 
                trangThai
            ); 
        }

        // Danh sách trạng thái
        List<String> trangThaiList = List.of(
            "Mới tiếp nhận", 
            "Chờ sửa chữa", 
            "Đang sửa chữa", 
            "Đã sửa xong", 
            "Đã trả khách"
        );

        model.addAttribute("results", results);
        model.addAttribute("query", query);
        model.addAttribute("trangThaiSelected", trangThai); 
        model.addAttribute("trangThaiList", trangThaiList); 
        model.addAttribute("pageTitle", "Tra cứu sửa chữa");
        model.addAttribute("currentPath", request.getServletPath()); 
        
        // *** TRẢ VỀ TÊN FILE TRA CỨU TRỰC TIẾP (GIỐNG NHƯ YÊU CẦU BAN ĐẦU) ***
        return "receptionist/tra-cuu-form";
    }

    @GetMapping("/tra-cuu/{id}")
    public String showDetailPage(@PathVariable("id") Integer id, Model model, HttpServletRequest request) {
        Optional<PhieuSuaChua> phieuOptional = phieuSuaChuaRepository.findById(id);
        if (phieuOptional.isEmpty()) {
            return "redirect:/receptionist/tra-cuu?error=notfound";
        }

        model.addAttribute("phieu", phieuOptional.get());
        model.addAttribute("pageTitle", "Chi tiết Phiếu #" + id);
        model.addAttribute("currentPath", request.getServletPath()); // Giữ currentPath cho chi tiết phiếu
        
        // Trả về tên file HTML trực tiếp
        return "receptionist/chi-tiet-phieu";
    }

    // ===================================
    // 4. TRẢ THIẾT BỊ & THANH TOÁN - /receptionist/tra-thiet-bi
    // ===================================

    @GetMapping("/tra-thiet-bi")
    public String showListPage(Model model, HttpServletRequest request) {
        List<PhieuSuaChua> phieuCanTra = phieuSuaChuaRepository.findByTrangThai("Đã sửa xong");
        model.addAttribute("phieuList", phieuCanTra);
        model.addAttribute("pageTitle", "Trả thiết bị & Thanh toán");
        model.addAttribute("currentPath", request.getServletPath()); 
        
        // Trả về tên file HTML trực tiếp
        return "receptionist/tra-thiet-bi-list";
    }

    @PostMapping("/tra-thiet-bi/{id}/thanh-toan")
    public String processPayment(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            PhieuSuaChua phieuSuaChua = phieuSuaChuaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Mã phiếu không hợp lệ: " + id));

            phieuSuaChua.setTrangThai("Đã trả khách");
            phieuSuaChua.setNgayTra(OffsetDateTime.now()); 
            phieuSuaChuaRepository.save(phieuSuaChua);

            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã xác nhận trả thiết bị cho phiếu **#" + id + "** thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thanh toán: " + e.getMessage());
        }

        return "redirect:/receptionist/tra-thiet-bi";
    }
}