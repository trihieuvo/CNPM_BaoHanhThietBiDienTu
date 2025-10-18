package com.baohanh.trungtambaohanh.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LinhKienDetailDto {
    private String tenLinhKien;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
}