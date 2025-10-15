package com.baohanh.trungtambaohanh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "chi_tiet_sua_chua", schema = "release")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietSuaChua {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_chi_tiet")
    private Integer maChiTiet;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_phieu", nullable = false)
    private PhieuSuaChua phieuSuaChua;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_linh_kien", nullable = false)
    private LinhKien linhKien;
    
    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;
    
    @Column(name = "don_gia", nullable = false, precision = 38, scale = 2)
    private BigDecimal donGia;
    
    @Column(name = "thanh_tien", precision = 38, scale = 2)
    private BigDecimal thanhTien;
    
    // Tính thành tiền tự động
    @PrePersist
    @PreUpdate
    public void calculateThanhTien() {
        if (soLuong != null && donGia != null) {
            this.thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));
        }
    }
}