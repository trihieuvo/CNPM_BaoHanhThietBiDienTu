package com.baohanh.trungtambaohanh.security;

import com.baohanh.trungtambaohanh.entity.TaiKhoan;
import com.baohanh.trungtambaohanh.repository.TaiKhoanRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TaiKhoanRepository taiKhoanRepository;

    public CustomUserDetailsService(TaiKhoanRepository taiKhoanRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với tên đăng nhập: " + username));

        // Kiểm tra tài khoản có bị vô hiệu hóa không
        if (!taiKhoan.isTrangThai()) {
            throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa: " + username);
        }

        // Tạo role với prefix ROLE_
        String roleName = "ROLE_" + taiKhoan.getVaiTro().getTenVaiTro();
        
        return User.builder()
                .username(taiKhoan.getTenDangNhap())
                .password(taiKhoan.getMatKhauHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(roleName)))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!taiKhoan.isTrangThai())
                .build();
    }
}