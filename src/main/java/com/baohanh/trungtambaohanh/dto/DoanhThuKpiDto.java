package com.baohanh.trungtambaohanh.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DoanhThuKpiDto {
    private BigDecimal tongDoanhThu = BigDecimal.ZERO;
    private long tongSoPhieuHoanThanh = 0;
    private BigDecimal doanhThuTrungBinh = BigDecimal.ZERO;
    private String linhKienDoanhThuCaoNhat = "N/A";
}