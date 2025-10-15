package com.baohanh.trungtambaohanh.service;

import com.baohanh.trungtambaohanh.dto.DangKyDto;
import com.baohanh.trungtambaohanh.entity.KhachHang;
import com.baohanh.trungtambaohanh.entity.TaiKhoan;
import com.baohanh.trungtambaohanh.entity.VaiTro;
import com.baohanh.trungtambaohanh.repository.KhachHangRepository;
import com.baohanh.trungtambaohanh.repository.TaiKhoanRepository;
import com.baohanh.trungtambaohanh.repository.VaiTroRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class TaiKhoanService {
    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;

    public TaiKhoanService(TaiKhoanRepository taiKhoanRepository, 
                          VaiTroRepository vaiTroRepository, 
                          KhachHangRepository khachHangRepository, 
                          PasswordEncoder passwordEncoder) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.khachHangRepository = khachHangRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void dangKyKhachHang(DangKyDto dangKyDto) {
        // Kiểm tra tên đăng nhập đã tồn tại
        if (taiKhoanRepository.findByTenDangNhap(dangKyDto.getTenDangNhap()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        // 1. Tìm vai trò CUSTOMER
        VaiTro vaiTro = vaiTroRepository.findByTenVaiTro("Khách hàng")
                .orElseThrow(() -> new RuntimeException("Vai trò 'Khách hàng' không tồn tại trong database. Vui lòng chạy script khởi tạo dữ liệu."));

        // 2. Tạo TaiKhoan
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(dangKyDto.getTenDangNhap());
        taiKhoan.setMatKhauHash(passwordEncoder.encode(dangKyDto.getMatKhau()));
        taiKhoan.setNgayTao(OffsetDateTime.now());
        taiKhoan.setTrangThai(true);
        taiKhoan.setVaiTro(vaiTro);
        
        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

        // 3. Tạo KhachHang
        KhachHang khachHang = new KhachHang();
        khachHang.setHoTen(dangKyDto.getHoTen());
        khachHang.setSoDienThoai(dangKyDto.getSoDienThoai());
        khachHang.setEmail(dangKyDto.getEmail());
        khachHang.setDiaChi(dangKyDto.getDiaChi());
        khachHang.setTaiKhoan(savedTaiKhoan);
        
        khachHangRepository.save(khachHang);
    }
}