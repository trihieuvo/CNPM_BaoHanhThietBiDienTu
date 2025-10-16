package com.baohanh.trungtambaohanh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinhKienSuDungDto {
    private String tenLinhKien;
    private Long soLuongDaDung;
    private Long soPhieuDaDung;
}