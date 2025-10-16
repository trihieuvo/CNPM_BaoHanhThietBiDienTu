package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    // Tìm phiếu theo kỹ thuật viên
    List<PhieuSuaChua> findByKyThuatVien_MaNV(Integer maNV);
    
    // Tìm phiếu theo khách hàng
    List<PhieuSuaChua> findByKhachHang_MaKH(Integer maKH);
    
    // Tìm phiếu theo khách hàng, sắp xếp theo ngày tiếp nhận giảm dần
    List<PhieuSuaChua> findByKhachHang_MaKHOrderByNgayTiepNhanDesc(Integer maKH);
    
    Page<PhieuSuaChua> findByKyThuatVien_MaNV(Integer maNV, Pageable pageable);

    // MỚI: Thêm phương thức tìm kiếm theo từ khóa cho KTV
    @Query("SELECT p FROM PhieuSuaChua p WHERE p.kyThuatVien.maNV = :maNV " +
           "AND (CAST(p.maPhieu AS string) LIKE %:keyword% " +
           "OR LOWER(p.khachHang.hoTen) LIKE %:keyword% " +
           "OR LOWER(p.thietBi.model) LIKE %:keyword%)")
    Page<PhieuSuaChua> searchByKeywordForTechnician(@Param("maNV") Integer maNV, @Param("keyword") String keyword, Pageable pageable);
    
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
    
    @Query("SELECT p FROM PhieuSuaChua p WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.khachHang.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.thietBi.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(p.maPhieu AS string) LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:status IS NULL OR :status = '' OR p.trangThai = :status)")
     // THAY ĐỔI: Chuyển từ List sang Page và thêm Pageable
     Page<PhieuSuaChua> searchAndFilter(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);
    

     @Query("SELECT p FROM PhieuSuaChua p WHERE p.trangThai = 'Đã trả khách' " +
            "AND p.ngayHoanThanh BETWEEN :startDate AND :endDate " +
            "AND (:ktvId IS NULL OR p.kyThuatVien.maNV = :ktvId)")
     List<PhieuSuaChua> findCompletedTicketsInDateRange(@Param("startDate") OffsetDateTime startDate, 
                                                       @Param("endDate") OffsetDateTime endDate,
                                                       @Param("ktvId") Integer ktvId);

    @Query("SELECT FUNCTION('date_trunc', 'day', p.ngayHoanThanh), SUM(p.tongChiPhi) " +
            "FROM PhieuSuaChua p " +
            "WHERE p.trangThai = 'Đã trả khách' AND p.ngayHoanThanh BETWEEN :startDate AND :endDate " +
            "AND (:ktvId IS NULL OR p.kyThuatVien.maNV = :ktvId) " +
            "GROUP BY FUNCTION('date_trunc', 'day', p.ngayHoanThanh) " +
            "ORDER BY FUNCTION('date_trunc', 'day', p.ngayHoanThanh)")
     List<Object[]> getRevenueByDay(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("ktvId") Integer ktvId);

     @Query("SELECT ltb.tenLoaiTB, SUM(p.tongChiPhi) " +
             "FROM PhieuSuaChua p JOIN p.thietBi tb JOIN tb.loaiThietBi ltb " +
             "WHERE p.trangThai = 'Đã trả khách' AND p.ngayHoanThanh BETWEEN :startDate AND :endDate " +
             "AND (:ktvId IS NULL OR p.kyThuatVien.maNV = :ktvId) " +
             "GROUP BY ltb.tenLoaiTB")
      List<Object[]> getRevenueByDeviceType(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("ktvId") Integer ktvId);

      @Query("SELECT p.trangThai, COUNT(p) FROM PhieuSuaChua p " +
             "WHERE p.ngayTiepNhan BETWEEN :startDate AND :endDate " +
             "AND (:ktvId IS NULL OR p.kyThuatVien.maNV = :ktvId) " +
             "AND (:loaiThietBiId IS NULL OR p.thietBi.loaiThietBi.maLoaiTB = :loaiThietBiId) " +
             "GROUP BY p.trangThai")
      List<Object[]> countTicketsByStatus(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("ktvId") Integer ktvId, @Param("loaiThietBiId") Integer loaiThietBiId);

      @Query("SELECT nv.hoTen, COUNT(p) FROM PhieuSuaChua p JOIN p.kyThuatVien nv " +
             "WHERE p.trangThai = 'Đã trả khách' AND p.ngayHoanThanh BETWEEN :startDate AND :endDate " +
             "AND (:loaiThietBiId IS NULL OR p.thietBi.loaiThietBi.maLoaiTB = :loaiThietBiId) " +
             "GROUP BY nv.hoTen")
      List<Object[]> countCompletedTicketsByTechnician(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("loaiThietBiId") Integer loaiThietBiId);

      @Query("SELECT p FROM PhieuSuaChua p WHERE p.ngayTiepNhan IS NOT NULL AND p.ngayHoanThanh IS NOT NULL " +
             "AND p.ngayHoanThanh BETWEEN :startDate AND :endDate " +
             "AND (:ktvId IS NULL OR p.kyThuatVien.maNV = :ktvId) " +
             "AND (:loaiThietBiId IS NULL OR p.thietBi.loaiThietBi.maLoaiTB = :loaiThietBiId)")
      List<PhieuSuaChua> findTicketsForAverageTime(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("ktvId") Integer ktvId, @Param("loaiThietBiId") Integer loaiThietBiId);
}