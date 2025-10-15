package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.PasswordResetToken;
import com.baohanh.trungtambaohanh.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    PasswordResetToken findByToken(String token);
    
    // THÊM PHƯƠNG THỨC NÀY
    Optional<PasswordResetToken> findByTaiKhoan_TenDangNhap(String tenDangNhap);
}