package com.baohanh.trungtambaohanh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "khieu_nai", schema = "release")
public class KhieuNai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_khieu_nai")
    private Integer maKhieuNai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_kh", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_phieu", nullable = false)
    private PhieuSuaChua phieuSuaChua;

    @Column(name = "tieu_de", nullable = false, length = 255)
    private String tieuDe;

    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "ngay_gui", nullable = false)
    private OffsetDateTime ngayGui;

    @Column(name = "trang_thai", nullable = false, length = 50)
    private String trangThai;
}