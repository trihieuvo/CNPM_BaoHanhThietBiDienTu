package com.baohanh.trungtambaohanh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "LoaiThietBi", schema = "release")
public class LoaiThietBi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLoaiTB")
    private Integer maLoaiTB;

    @Column(name = "TenLoaiTB", unique = true, nullable = false)
    private String tenLoaiTB;
}