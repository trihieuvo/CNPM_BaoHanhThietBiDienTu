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
@Table(name = "TaiKhoan", schema = "release")
public class TaiKhoan {

    @Id
    @Column(name = "TenDangNhap")
    private String tenDangNhap;

    @Column(name = "MatKhauHash", nullable = false)
    private String matKhauHash;

    @Column(name = "TrangThai")
    private boolean trangThai;

    @Column(name = "NgayTao")
    private OffsetDateTime ngayTao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaVaiTro", nullable = false)
    private VaiTro vaiTro;
}