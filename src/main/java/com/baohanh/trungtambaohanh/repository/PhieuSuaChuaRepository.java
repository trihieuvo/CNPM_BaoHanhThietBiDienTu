package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PhieuSuaChuaRepository extends JpaRepository<PhieuSuaChua, Integer> {

    // Các phương thức cơ bản
    long countByTrangThai(String trangThai);
    List<PhieuSuaChua> findByTrangThai(String trangThai);
    List<PhieuSuaChua> findByKyThuatVien_MaNV(Integer maNV);
    List<PhieuSuaChua> findByKhachHang_MaKH(Integer maKH);
    Page<PhieuSuaChua> findByKyThuatVien_MaNV(Integer maNV, Pageable pageable);
    
    // Phương thức CustomerController cần
    List<PhieuSuaChua> findByKhachHang_MaKHOrderByNgayTiepNhanDesc(Integer maKH);

    // Phiên bản không phân trang cho TraCuuController
    @Query("SELECT p FROM PhieuSuaChua p WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.khachHang.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.khachHang.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(p.maPhieu AS string) LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:status IS NULL OR :status = '' OR p.trangThai = :status)")
    List<PhieuSuaChua> searchAndFilter(@Param("keyword") String keyword, @Param("status") String status);

    // Phiên bản có phân trang cho ManagerController
    @Query("SELECT p FROM PhieuSuaChua p WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.khachHang.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.thietBi.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(p.maPhieu AS string) LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:status IS NULL OR :status = '' OR p.trangThai = :status)")
    Page<PhieuSuaChua> searchAndFilter(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);

    // Phương thức TechnicianController cần
    @Query("SELECT p FROM PhieuSuaChua p WHERE p.kyThuatVien.maNV = :maNV " +
           "AND (CAST(p.maPhieu AS string) LIKE %:keyword% " +
           "OR LOWER(p.khachHang.hoTen) LIKE %:keyword% " +
           "OR LOWER(p.thietBi.model) LIKE %:keyword%)")
    Page<PhieuSuaChua> searchByKeywordForTechnician(@Param("maNV") Integer maNV, @Param("keyword") String keyword, Pageable pageable);

    // === CÁC HÀM CHO BÁO CÁO (BaoCaoServiceImpl) ===
    @Query("SELECT SUM(p.tongChiPhi) FROM PhieuSuaChua p WHERE p.trangThai = 'Đã trả khách'")
    BigDecimal findTotalRevenue();

    @Query("SELECT p FROM PhieuSuaChua p WHERE p.ngayTiepNhan IS NOT NULL AND p.ngayHoanThanh IS NOT NULL AND p.trangThai IN ('Đã sửa xong', 'Đã trả khách')")
    List<PhieuSuaChua> findAllCompletedWithTimestamps();
    
    @Query("SELECT p FROM PhieuSuaChua p WHERE p.trangThai = 'Đã trả khách' " +
            "AND p.ngayHoanThanh BETWEEN :startDate AND :endDate " +
            "AND (:ktvId IS NULL OR p.kyThuatVien.maNV = :ktvId)")
    List<PhieuSuaChua> findCompletedTicketsInDateRange(@Param("startDate") OffsetDateTime startDate,
                                                      @Param("endDate") OffsetDateTime endDate,
                                                      @Param("ktvId") Integer ktvId);

    @Query("SELECT FUNCTION('date_trunc', 'day', p.ngayHoanThanh), SUM(p.tongChiPhi) FROM PhieuSuaChua p WHERE p.trangThai = 'Đã trả khách' AND p.ngayHoanThanh BETWEEN :startDate AND :endDate AND (:ktvId IS NULL OR p.kyThuatVien.maNV = :ktvId) GROUP BY FUNCTION('date_trunc', 'day', p.ngayHoanThanh) ORDER BY FUNCTION('date_trunc', 'day', p.ngayHoanThanh)")
    List<Object[]> getRevenueByDay(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("ktvId") Integer ktvId);

    @Query("SELECT ltb.tenLoaiTB, SUM(p.tongChiPhi) FROM PhieuSuaChua p JOIN p.thietBi tb JOIN tb.loaiThietBi ltb WHERE p.trangThai = 'Đã trả khách' AND p.ngayHoanThanh BETWEEN :startDate AND :endDate AND (:ktvId IS NULL OR p.kyThuatVien.maNV = :ktvId) GROUP BY ltb.tenLoaiTB")
    List<Object[]> getRevenueByDeviceType(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("ktvId") Integer ktvId);

    @Query("SELECT p.trangThai, COUNT(p) FROM PhieuSuaChua p WHERE p.ngayTiepNhan BETWEEN :startDate AND :endDate AND (:ktvId IS NULL OR p.kyThuatVien.maNV = :ktvId) AND (:loaiThietBiId IS NULL OR p.thietBi.loaiThietBi.maLoaiTB = :loaiThietBiId) GROUP BY p.trangThai")
    List<Object[]> countTicketsByStatus(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("ktvId") Integer ktvId, @Param("loaiThietBiId") Integer loaiThietBiId);

    @Query("SELECT nv.hoTen, COUNT(p) FROM PhieuSuaChua p JOIN p.kyThuatVien nv WHERE p.trangThai = 'Đã trả khách' AND p.ngayHoanThanh BETWEEN :startDate AND :endDate AND (:loaiThietBiId IS NULL OR p.thietBi.loaiThietBi.maLoaiTB = :loaiThietBiId) GROUP BY nv.hoTen")
    List<Object[]> countCompletedTicketsByTechnician(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("loaiThietBiId") Integer loaiThietBiId);

    @Query("SELECT p FROM PhieuSuaChua p WHERE p.ngayTiepNhan IS NOT NULL AND p.ngayHoanThanh IS NOT NULL AND p.ngayHoanThanh BETWEEN :startDate AND :endDate AND (:ktvId IS NULL OR p.kyThuatVien.maNV = :ktvId) AND (:loaiThietBiId IS NULL OR p.thietBi.loaiThietBi.maLoaiTB = :loaiThietBiId)")
    List<PhieuSuaChua> findTicketsForAverageTime(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, @Param("ktvId") Integer ktvId, @Param("loaiThietBiId") Integer loaiThietBiId);
    
    @Query("SELECT p FROM PhieuSuaChua p ORDER BY p.id DESC")
    List<PhieuSuaChua> findTop10ByOrderByIdDesc();  // Limit 10 ở service nếu cần
    
    
    
    
}