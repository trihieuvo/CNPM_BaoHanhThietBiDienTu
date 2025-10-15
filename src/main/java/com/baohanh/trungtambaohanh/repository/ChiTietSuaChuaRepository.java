package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.ChiTietSuaChua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ChiTietSuaChuaRepository extends JpaRepository<ChiTietSuaChua, Integer> {
    
    // Tìm tất cả chi tiết theo mã phiếu
    List<ChiTietSuaChua> findByPhieuSuaChua_MaPhieu(Integer maPhieu);
    
    // Xóa tất cả chi tiết của một phiếu
    void deleteByPhieuSuaChua_MaPhieu(Integer maPhieu);
    
    @Query("SELECT ct.linhKien.tenLinhKien, SUM(ct.thanhTien) as totalRevenue " +
            "FROM ChiTietSuaChua ct " +
            "WHERE ct.phieuSuaChua.trangThai = 'Đã trả khách' AND ct.phieuSuaChua.ngayHoanThanh BETWEEN :startDate AND :endDate " +
            "GROUP BY ct.linhKien.tenLinhKien " +
            "ORDER BY totalRevenue DESC")
     List<Object[]> findTopRevenuePartInDateRange(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, Pageable pageable);
}