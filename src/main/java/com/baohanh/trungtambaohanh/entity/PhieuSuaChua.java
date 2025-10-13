package com.baohanh.trungtambaohanh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PhieuSuaChua", schema = "release")
public class PhieuSuaChua {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhieu")
    private Integer maPhieu;

    @Column(name = "NgayTiepNhan")
    private OffsetDateTime ngayTiepNhan;

    @Column(name = "NgayTraDuKien")
    private OffsetDateTime ngayTraDuKien;

    @Column(name = "NgayHoanThanh")
    private OffsetDateTime ngayHoanThanh;

    @Column(name = "TinhTrangTiepNhan", nullable = false, columnDefinition = "TEXT")
    private String tinhTrangTiepNhan;

    @Column(name = "GhiChuKyThuat", columnDefinition = "TEXT")
    private String ghiChuKyThuat;

    @Column(name = "TrangThai", nullable = false)
    private String trangThai;

    @Column(name = "TongChiPhi")
    private BigDecimal tongChiPhi;

    @ManyToOne
    @JoinColumn(name = "MaKH", nullable = false)
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "MaThietBi", nullable = false)
    private ThietBi thietBi;

    @ManyToOne
    @JoinColumn(name = "MaNhanVienTiepNhan", nullable = false)
    private NhanVien nhanVienTiepNhan;

    @ManyToOne
    @JoinColumn(name = "MaKyThuatVien")
    private NhanVien kyThuatVien;
}