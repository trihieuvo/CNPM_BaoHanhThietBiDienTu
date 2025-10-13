package com.baohanh.trungtambaohanh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ThietBi", schema = "release")
public class ThietBi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaThietBi")
    private Integer maThietBi;

    @Column(name = "HangSanXuat")
    private String hangSanXuat;

    @Column(name = "Model", nullable = false)
    private String model;

    @Column(name = "SoSerial", unique = true)
    private String soSerial;

    @Column(name = "MoTa")
    private String moTa;

    @ManyToOne
    @JoinColumn(name = "MaKH", nullable = false)
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "MaLoaiTB", nullable = false)
    private LoaiThietBi loaiThietBi;
}