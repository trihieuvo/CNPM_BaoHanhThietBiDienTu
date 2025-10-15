package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
    
    
    @Query("SELECT p FROM PhieuSuaChua p WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.khachHang.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.thietBi.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(p.maPhieu AS string) LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:status IS NULL OR :status = '' OR p.trangThai = :status)")
     List<PhieuSuaChua> searchAndFilter(@Param("keyword") String keyword, @Param("status") String status);
    
 // Lấy các phiếu đã trả khách trong một khoảng thời gian
    @Query("SELECT p FROM PhieuSuaChua p WHERE p.trangThai = 'Đã trả khách' " +
            "AND p.ngayHoanThanh BETWEEN :startDate AND :endDate " +
            "AND (:ktvId IS NULL OR p.kyThuatVien.maNV = :ktvId)")
     List<PhieuSuaChua> findCompletedTicketsInDateRange(@Param("startDate") OffsetDateTime startDate, 
                                                       @Param("endDate") OffsetDateTime endDate,
                                                       @Param("ktvId") Integer ktvId);

    // Lấy dữ liệu doanh thu theo ngày cho biểu đồ đường
    @Query("SELECT FUNCTION('date_trunc', 'day', p.ngayHoanThanh), SUM(p.tongChiPhi) " +
            "FROM PhieuSuaChua p " +
            "WHERE p.trangThai = 'Đã trả khách' AND p.ngayHoanThanh BETWEEN :startDate AND :endDate " +
            "AND (:ktvId IS NULL OR p.kyThuatVien.maNV = :ktvId) " + // THÊM DÒNG NÀY
            "GROUP BY FUNCTION('date_trunc', 'day', p.ngayHoanThanh) " +
            "ORDER BY FUNCTION('date_trunc', 'day', p.ngayHoanThanh)")
     List<Object[]> getRevenueByDay(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("ktvId") Integer ktvId);
    
    // Lấy dữ liệu doanh thu theo loại thiết bị cho biểu đồ tròn
     @Query("SELECT ltb.tenLoaiTB, SUM(p.tongChiPhi) " +
             "FROM PhieuSuaChua p JOIN p.thietBi tb JOIN tb.loaiThietBi ltb " +
             "WHERE p.trangThai = 'Đã trả khách' AND p.ngayHoanThanh BETWEEN :startDate AND :endDate " +
             "AND (:ktvId IS NULL OR p.kyThuatVien.maNV = :ktvId) " + // THÊM DÒNG NÀY
             "GROUP BY ltb.tenLoaiTB")
      List<Object[]> getRevenueByDeviceType(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("ktvId") Integer ktvId);
}