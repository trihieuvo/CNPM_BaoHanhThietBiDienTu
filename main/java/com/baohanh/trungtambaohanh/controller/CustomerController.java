package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.entity.KhachHang;
import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import com.baohanh.trungtambaohanh.entity.ThietBi;
import com.baohanh.trungtambaohanh.repository.KhachHangRepository;
import com.baohanh.trungtambaohanh.repository.PhieuSuaChuaRepository;
import com.baohanh.trungtambaohanh.repository.ThietBiRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final KhachHangRepository khachHangRepository;
    private final ThietBiRepository thietBiRepository;
    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerController(KhachHangRepository khachHangRepository,
                            ThietBiRepository thietBiRepository,
                            PhieuSuaChuaRepository phieuSuaChuaRepository,
                            PasswordEncoder passwordEncoder) {
        this.khachHangRepository = khachHangRepository;
        this.thietBiRepository = thietBiRepository;
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Dashboard - Trang chủ khách hàng
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByTaiKhoan_TenDangNhap(principal.getName());
        
        if (khachHangOpt.isPresent()) {
            KhachHang khachHang = khachHangOpt.get();
            
            // Lấy danh sách thiết bị của khách hàng
            List<ThietBi> danhSachThietBi = thietBiRepository.findByKhachHang_MaKH(khachHang.getMaKH());
            
            // Lấy danh sách phiếu sửa chữa
            List<PhieuSuaChua> danhSachPhieu = phieuSuaChuaRepository.findByKhachHang_MaKH(khachHang.getMaKH());
            
            model.addAttribute("khachHang", khachHang);
            model.addAttribute("danhSachThietBi", danhSachThietBi);
            model.addAttribute("danhSachPhieu", danhSachPhieu);
            model.addAttribute("soThietBi", danhSachThietBi.size());
            model.addAttribute("soPhieuSua", danhSachPhieu.size());
            
            return "customer/dashboard";
        }
        
        return "redirect:/login";
    }

    // Xem lịch sử bảo hành
    @GetMapping("/lich-su-bao-hanh")
    public String xemLichSuBaoHanh(Model model, Principal principal) {
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByTaiKhoan_TenDangNhap(principal.getName());
        
        if (khachHangOpt.isPresent()) {
            KhachHang khachHang = khachHangOpt.get();
            List<PhieuSuaChua> danhSachPhieu = phieuSuaChuaRepository.findByKhachHang_MaKHOrderByNgayTiepNhanDesc(khachHang.getMaKH());
            
            model.addAttribute("danhSachPhieu", danhSachPhieu);
            model.addAttribute("khachHang", khachHang);
            
            return "customer/lich-su-bao-hanh";
        }
        
        return "redirect:/login";
    }

    // Xem chi tiết phiếu sửa chữa
    @GetMapping("/phieu-sua-chua/{id}")
    public String xemChiTietPhieu(@PathVariable("id") Integer id, Model model, Principal principal) {
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByTaiKhoan_TenDangNhap(principal.getName());
        Optional<PhieuSuaChua> phieuOpt = phieuSuaChuaRepository.findById(id);
        
        if (khachHangOpt.isPresent() && phieuOpt.isPresent()) {
            PhieuSuaChua phieu = phieuOpt.get();
            
            // Kiểm tra phiếu có thuộc về khách hàng này không
            if (phieu.getKhachHang().getMaKH().equals(khachHangOpt.get().getMaKH())) {
                model.addAttribute("phieu", phieu);
                model.addAttribute("chiTietList", phieu.getChiTietSuaChuaList());
                return "customer/chi-tiet-phieu";
            }
        }
        
        return "redirect:/customer/dashboard";
    }

    // Tra cứu tình trạng sửa chữa
    @GetMapping("/tra-cuu-sua-chua")
    public String traCuuSuaChua(@RequestParam(value = "maPhieu", required = false) Integer maPhieu,
                                Model model, Principal principal) {
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByTaiKhoan_TenDangNhap(principal.getName());
        
        if (khachHangOpt.isPresent()) {
            KhachHang khachHang = khachHangOpt.get();
            
            if (maPhieu != null) {
                Optional<PhieuSuaChua> phieuOpt = phieuSuaChuaRepository.findById(maPhieu);
                
                if (phieuOpt.isPresent() && phieuOpt.get().getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
                    model.addAttribute("phieu", phieuOpt.get());
                } else {
                    model.addAttribute("errorMessage", "Không tìm thấy phiếu sửa chữa!");
                }
            }
            
            return "customer/tra-cuu-sua-chua";
        }
        
        return "redirect:/login";
    }

    // Xem thông tin tài khoản
    @GetMapping("/thong-tin-tai-khoan")
    public String xemThongTinTaiKhoan(Model model, Principal principal) {
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByTaiKhoan_TenDangNhap(principal.getName());
        
        if (khachHangOpt.isPresent()) {
            model.addAttribute("khachHang", khachHangOpt.get());
            return "customer/thong-tin-tai-khoan";
        }
        
        return "redirect:/login";
    }

    // Cập nhật thông tin tài khoản
    @PostMapping("/cap-nhat-thong-tin")
    @Transactional
    public String capNhatThongTin(@RequestParam("hoTen") String hoTen,
                                 @RequestParam("soDienThoai") String soDienThoai,
                                 @RequestParam("email") String email,
                                 @RequestParam("diaChi") String diaChi,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            Optional<KhachHang> khachHangOpt = khachHangRepository.findByTaiKhoan_TenDangNhap(principal.getName());
            
            if (khachHangOpt.isPresent()) {
                KhachHang khachHang = khachHangOpt.get();
                khachHang.setHoTen(hoTen);
                khachHang.setSoDienThoai(soDienThoai);
                khachHang.setEmail(email);
                khachHang.setDiaChi(diaChi);
                
                khachHangRepository.save(khachHang);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
        }
        
        return "redirect:/customer/thong-tin-tai-khoan";
    }

    // Đổi mật khẩu
    @GetMapping("/doi-mat-khau")
    public String doiMatKhauPage(Principal principal, Model model) {
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByTaiKhoan_TenDangNhap(principal.getName());
        
        if (khachHangOpt.isPresent()) {
            model.addAttribute("khachHang", khachHangOpt.get());
            return "customer/doi-mat-khau";
        }
        
        return "redirect:/login";
    }

    @PostMapping("/doi-mat-khau")
    @Transactional
    public String doiMatKhau(@RequestParam("matKhauCu") String matKhauCu,
                            @RequestParam("matKhauMoi") String matKhauMoi,
                            @RequestParam("xacNhanMatKhau") String xacNhanMatKhau,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            Optional<KhachHang> khachHangOpt = khachHangRepository.findByTaiKhoan_TenDangNhap(principal.getName());
            
            if (khachHangOpt.isPresent()) {
                KhachHang khachHang = khachHangOpt.get();
                
                // Kiểm tra mật khẩu cũ
                if (!passwordEncoder.matches(matKhauCu, khachHang.getTaiKhoan().getMatKhauHash())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu cũ không đúng!");
                    return "redirect:/customer/doi-mat-khau";
                }
                
                // Kiểm tra mật khẩu mới khớp
                if (!matKhauMoi.equals(xacNhanMatKhau)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới không khớp!");
                    return "redirect:/customer/doi-mat-khau";
                }
                
                // Cập nhật mật khẩu
                khachHang.getTaiKhoan().setMatKhauHash(passwordEncoder.encode(matKhauMoi));
                khachHangRepository.save(khachHang);
                
                redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi đổi mật khẩu: " + e.getMessage());
        }
        
        return "redirect:/customer/doi-mat-khau";
    }

    // Gửi yêu cầu/khiếu nại
    @GetMapping("/khieu-nai")
    public String guiKhieuNaiPage(Principal principal, Model model) {
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByTaiKhoan_TenDangNhap(principal.getName());
        
        if (khachHangOpt.isPresent()) {
            model.addAttribute("khachHang", khachHangOpt.get());
            return "customer/gui-khieu-nai";
        }
        
        return "redirect:/login";
    }

    @PostMapping("/khieu-nai")
    public String guiKhieuNai(@RequestParam("tieuDe") String tieuDe,
                             @RequestParam("noiDung") String noiDung,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        // Tính năng này sẽ được mở rộng sau để lưu vào bảng khiếu nại
        redirectAttributes.addFlashAttribute("successMessage", "Đã gửi khiếu nại thành công! Chúng tôi sẽ xử lý trong thời gian sớm nhất.");
        return "redirect:/customer/dashboard";
    }
}