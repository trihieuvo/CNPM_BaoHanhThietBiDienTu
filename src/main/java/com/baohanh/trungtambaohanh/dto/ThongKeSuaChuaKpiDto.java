package com.baohanh.trungtambaohanh.dto;

import lombok.Data;

@Data
public class ThongKeSuaChuaKpiDto {
    private long tongPhieuTiepNhan;
    private long tongPhieuHoanThanh;
    private String thoiGianSuaTrungBinh = "N/A";
}