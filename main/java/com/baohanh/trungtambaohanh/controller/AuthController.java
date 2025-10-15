package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.dto.DangKyDto;
import com.baohanh.trungtambaohanh.service.TaiKhoanService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final TaiKhoanService taiKhoanService;

    public AuthController(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    @GetMapping("/")
    public String trangChu() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "register_success", required = false) String registerSuccess,
            Model model) {
        
        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không đúng!");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Đăng xuất thành công!");
        }
        if (registerSuccess != null) {
            model.addAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
        }
        
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new DangKyDto());
        return "register";
    }

    @PostMapping("/register/save")
    public String registration(@ModelAttribute("user") DangKyDto dangKyDto, 
                              RedirectAttributes redirectAttributes) {
        try {
            taiKhoanService.dangKyKhachHang(dangKyDto);
            return "redirect:/login?register_success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đăng ký thất bại: " + e.getMessage());
            redirectAttributes.addFlashAttribute("user", dangKyDto);
            return "redirect:/register";
        }
    }

    // Redirect user based on role after successful login
    @GetMapping("/home")
    public String home(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            String role = auth.getAuthority();
            
            if (role.equals("ROLE_Kỹ thuật viên")) {
                return "redirect:/technician/dashboard";
            } else if (role.equals("ROLE_Quản lý")) {
                return "redirect:/manager/dashboard";
            } else if (role.equals("ROLE_Nhân viên")) {
                return "redirect:/receptionist/dashboard";
            } else if (role.equals("ROLE_Khách hàng")) {
                return "redirect:/customer/dashboard";
            }
        }
        
        // Default redirect
        return "redirect:/";
    }
}