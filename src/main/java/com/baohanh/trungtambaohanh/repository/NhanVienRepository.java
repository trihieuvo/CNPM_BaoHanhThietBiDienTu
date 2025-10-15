package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    
    // Tìm nhân viên dựa vào Tên Đăng Nhập của tài khoản liên kết
    Optional<NhanVien> findByTaiKhoan_TenDangNhap(String tenDangNhap);
}