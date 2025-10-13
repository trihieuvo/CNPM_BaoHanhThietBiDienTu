package com.baohanh.trungtambaohanh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ChiTietSuaChua", schema = "release")
public class ChiTietSuaChua {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChiTiet")
    private Integer maChiTiet;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "DonGia", nullable = false)
    private BigDecimal donGia;

    @Column(name = "ThanhTien", nullable = false)
    private BigDecimal thanhTien;

    @ManyToOne
    @JoinColumn(name = "MaPhieu", nullable = false)
    private PhieuSuaChua phieuSuaChua;

    @ManyToOne
    @JoinColumn(name = "MaLinhKien", nullable = false)
    private LinhKien linhKien;
}