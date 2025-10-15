package com.baohanh.trungtambaohanh.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Data
@NoArgsConstructor
public class PasswordResetToken {

    private static final int EXPIRATION = 60 * 1; 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = TaiKhoan.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "ten_dang_nhap")
    private TaiKhoan taiKhoan;

    private OffsetDateTime expiryDate;

    public PasswordResetToken(String token, TaiKhoan taiKhoan) {
        this.token = token;
        this.taiKhoan = taiKhoan;
        this.expiryDate = OffsetDateTime.now().plusMinutes(EXPIRATION);
    }
}