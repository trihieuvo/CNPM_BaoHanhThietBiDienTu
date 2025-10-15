package com.baohanh.trungtambaohanh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "NhanVien", schema = "release")
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNV")
    private Integer maNV;

    @Column(name = "HoTen", nullable = false)
    private String hoTen;

    @Column(name = "SoDienThoai", unique = true)
    private String soDienThoai;

    @Column(name = "Email", unique = true)
    private String email;

    @OneToOne
    @JoinColumn(name = "TenDangNhap", referencedColumnName = "TenDangNhap", unique = true, nullable = false)
    private TaiKhoan taiKhoan;
}