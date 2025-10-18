package com.baohanh.trungtambaohanh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "phieu_sua_chua", schema = "release")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhieuSuaChua {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_phieu")
    private Integer maPhieu;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "makh")
    private KhachHang khachHang;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_thiet_bi")
    private ThietBi thietBi;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nhan_vien_tiep_nhan")
    private NhanVien nhanVienTiepNhan;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_ky_thuat_vien")
    private NhanVien kyThuatVien;
    
    @Column(name = "ngay_tiep_nhan")
    private OffsetDateTime ngayTiepNhan;
    
    @Column(name = "ngay_hoan_thanh")
    private OffsetDateTime ngayHoanThanh;
    
    @Column(name = "ngay_tra_du_kien")
    private OffsetDateTime ngayTraDuKien;
    
    @Column(name = "tinh_trang_tiep_nhan", columnDefinition = "TEXT")
    private String tinhTrangTiepNhan;
    
    @Column(name = "trang_thai")
    private String trangThai;
    
    @Column(name = "ghi_chu_ky_thuat", columnDefinition = "TEXT")
    private String ghiChuKyThuat;
    
    @Column(name = "tong_chi_phi", precision = 38, scale = 2)
    private BigDecimal tongChiPhi;
    
    
    // Quan hệ OneToMany với ChiTietSuaChua
    @OneToMany(mappedBy = "phieuSuaChua", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietSuaChua> chiTietSuaChuaList = new ArrayList<>();
    
    // Phương thức tính tổng chi phí từ các chi tiết
    public void tinhTongChiPhi() {
        this.tongChiPhi = chiTietSuaChuaList.stream()
                .map(ChiTietSuaChua::getThanhTien)
                .filter(thanhTien -> thanhTien != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Helper methods để thêm/xóa chi tiết
    public void addChiTiet(ChiTietSuaChua chiTiet) {
        chiTietSuaChuaList.add(chiTiet);
        chiTiet.setPhieuSuaChua(this);
    }
    
    public void removeChiTiet(ChiTietSuaChua chiTiet) {
        chiTietSuaChuaList.remove(chiTiet);
        chiTiet.setPhieuSuaChua(null);
    }
}