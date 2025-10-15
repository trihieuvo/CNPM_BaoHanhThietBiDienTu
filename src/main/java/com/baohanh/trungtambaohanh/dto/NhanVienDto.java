package com.baohanh.trungtambaohanh.dto;

import lombok.Data;

@Data
public class NhanVienDto {
    private Integer maNV;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String tenDangNhap;
    private String matKhau; 
    private Integer maVaiTro;
    private boolean trangThai = true; 
}