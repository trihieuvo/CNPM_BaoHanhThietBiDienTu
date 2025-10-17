package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.entity.KhachHang;
import com.baohanh.trungtambaohanh.entity.PasswordResetToken;
import com.baohanh.trungtambaohanh.entity.TaiKhoan;
import com.baohanh.trungtambaohanh.repository.KhachHangRepository;
import com.baohanh.trungtambaohanh.repository.PasswordResetTokenRepository;
import com.baohanh.trungtambaohanh.repository.TaiKhoanRepository;
import com.baohanh.trungtambaohanh.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class PasswordResetController {

    private final KhachHangRepository khachHangRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final TaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;

    // ... Constructor giữ nguyên ...
    public PasswordResetController(KhachHangRepository khachHangRepository, PasswordResetTokenRepository tokenRepository, EmailService emailService, TaiKhoanRepository taiKhoanRepository, PasswordEncoder passwordEncoder) {
        this.khachHangRepository = khachHangRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.taiKhoanRepository = taiKhoanRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    // THAY THẾ TOÀN BỘ PHƯƠNG THỨC NÀY
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String userEmail, RedirectAttributes redirectAttributes) {
        Optional<KhachHang> optionalKhachHang = khachHangRepository.findByEmail(userEmail);

        if (optionalKhachHang.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản nào với email này.");
            return "redirect:/forgot-password";
        }

        TaiKhoan taiKhoan = optionalKhachHang.get().getTaiKhoan();
        
        // Tìm token hiện có của tài khoản
        PasswordResetToken myToken = tokenRepository.findByTaiKhoan_TenDangNhap(taiKhoan.getTenDangNhap()).orElse(null);

        String token = UUID.randomUUID().toString();

        if (myToken == null) {
            // Nếu chưa có token, tạo mới
            myToken = new PasswordResetToken(token, taiKhoan);
        } else {
            // Nếu đã có, cập nhật token và ngày hết hạn
            myToken.setToken(token);
            myToken.setExpiryDate(OffsetDateTime.now().plusMinutes(60 * 24)); // Reset lại 24h
        }
        
        tokenRepository.save(myToken);

        try {
            emailService.sendPasswordResetEmail(userEmail, token);
            redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu thành công. Vui lòng kiểm tra email của bạn để đặt lại mật khẩu.");
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi gửi email. Vui lòng thử lại sau.");
        }
        
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().isBefore(OffsetDateTime.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Token không hợp lệ hoặc đã hết hạn.");
            return "redirect:/login";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }
    
    // ... các phương thức còn lại giữ nguyên ...
    @PostMapping("/reset-password")
    @Transactional
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       RedirectAttributes redirectAttributes) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().isBefore(OffsetDateTime.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Token không hợp lệ hoặc đã hết hạn.");
            return "redirect:/login";
        }

        TaiKhoan taiKhoan = resetToken.getTaiKhoan();
        taiKhoan.setMatKhauHash(passwordEncoder.encode(password));
        taiKhoanRepository.save(taiKhoan);
        tokenRepository.delete(resetToken);

        redirectAttributes.addFlashAttribute("successMessage", "Mật khẩu của bạn đã được đặt lại thành công. Vui lòng đăng nhập.");
        return "redirect:/login";
    }
}