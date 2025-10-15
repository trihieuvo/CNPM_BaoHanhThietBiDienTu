package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    
    // Tìm nhân viên dựa vào Tên Đăng Nhập của tài khoản liên kết
    Optional<NhanVien> findByTaiKhoan_TenDangNhap(String tenDangNhap);
    
    @Query("SELECT nv FROM NhanVien nv WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(nv.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(nv.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:vaiTroId IS NULL OR nv.taiKhoan.vaiTro.maVaiTro = :vaiTroId)")
    List<NhanVien> searchAndFilter(@Param("keyword") String keyword, @Param("vaiTroId") Integer vaiTroId);

    @Query("SELECT nv FROM NhanVien nv WHERE nv.taiKhoan.vaiTro.tenVaiTro = 'Kỹ thuật viên'")
    List<NhanVien> findAllTechnicians();
}