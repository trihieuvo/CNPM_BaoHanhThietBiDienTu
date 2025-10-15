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
@Table(name = "\"LinhKien\"", schema = "release")
public class LinhKien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"MaLinhKien\"")
    private Integer maLinhKien;

    @Column(name = "\"TenLinhKien\"", nullable = false)
    private String tenLinhKien;

    @Column(name = "\"DonGia\"", nullable = false)
    private BigDecimal donGia;

    @Column(name = "\"SoLuongTon\"")
    private Integer soLuongTon;

    @Column(name = "\"NhaCungCap\"")
    private String nhaCungCap;

    @Column(name = "\"MoTa\"")
    private String moTa;

    @ManyToOne
    @JoinColumn(name = "\"MaLoaiTBTuongThich\"")
    private LoaiThietBi loaiThietBiTuongThich;
}