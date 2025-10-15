package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PhieuSuaChuaRepository extends JpaRepository<PhieuSuaChua, Integer> {

    // Tính tổng doanh thu từ các phiếu đã trả khách
    @Query("SELECT SUM(p.tongChiPhi) FROM PhieuSuaChua p WHERE p.trangThai = 'Đã trả khách'")
    BigDecimal findTotalRevenue();

    // Đếm số phiếu theo trạng thái
    long countByTrangThai(String trangThai);
    
    // Tìm phiếu theo kỹ thuật viên
    List<PhieuSuaChua> findByKyThuatVien_MaNV(Integer maNV);
    
    // Tìm phiếu theo khách hàng
    List<PhieuSuaChua> findByKhachHang_MaKH(Integer maKH);
    
    // Tìm phiếu theo khách hàng, sắp xếp theo ngày tiếp nhận giảm dần
    List<PhieuSuaChua> findByKhachHang_MaKHOrderByNgayTiepNhanDesc(Integer maKH);
    
    Page<PhieuSuaChua> findByKyThuatVien_MaNV(Integer maNV, Pageable pageable);
    
    // Lấy các phiếu đã hoàn thành và có đủ ngày nhận/ngày xong để tính thời gian
    @Query("SELECT p FROM PhieuSuaChua p WHERE p.ngayTiepNhan IS NOT NULL AND p.ngayHoanThanh IS NOT NULL AND p.trangThai IN ('Đã sửa xong', 'Đã trả khách')")
    List<PhieuSuaChua> findAllCompletedWithTimestamps();
}