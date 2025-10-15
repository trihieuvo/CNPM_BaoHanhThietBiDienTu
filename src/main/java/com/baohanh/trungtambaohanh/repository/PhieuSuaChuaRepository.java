package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PhieuSuaChuaRepository extends JpaRepository<PhieuSuaChua, Integer> {

    // Tính tổng doanh thu từ các phiếu đã trả khách
    @Query("SELECT SUM(p.tongChiPhi) FROM PhieuSuaChua p WHERE p.trangThai = 'Đã trả khách'")
    BigDecimal findTotalRevenue();

    // Đếm số phiếu theo trạng thái
    long countByTrangThai(String trangThai);
    List<PhieuSuaChua> findByKyThuatVien_MaNV(Integer maNV);
    
    // Lấy các phiếu đã hoàn thành và có đủ ngày nhận/ngày xong để tính thời gian
    @Query("SELECT p FROM PhieuSuaChua p WHERE p.ngayTiepNhan IS NOT NULL AND p.ngayHoanThanh IS NOT NULL AND p.trangThai IN ('Đã sửa xong', 'Đã trả khách')")
    List<PhieuSuaChua> findAllCompletedWithTimestamps();
    
    
    
}