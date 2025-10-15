package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    Optional<KhachHang> findByTaiKhoan_TenDangNhap(String tenDangNhap);
    Optional<KhachHang> findBySoDienThoai(String soDienThoai);
    Optional<KhachHang> findByEmail(String email);
    
    List<KhachHang> findBySoDienThoaiContainingIgnoreCase(String soDienThoai);

}