package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    // Có thể thêm các phương thức truy vấn tùy chỉnh ở đây nếu cần
}