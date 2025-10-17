package com.baohanh.trungtambaohanh.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class PhieuSuaChuaDetailDto {
    private Integer maPhieu;
    private String trangThai;
    private String khachHangHoTen;
    private String khachHangSdt;
    private String thietBiHangSanXuat;
    private String thietBiModel;
    private String thietBiSoSerial;
    private String ktvHoTen;
    private OffsetDateTime ngayTiepNhan;
    private String tinhTrangTiepNhan;
    private BigDecimal tongChiPhi;
    private List<LinhKienDetailDto> chiTietSuaChuaList;
}