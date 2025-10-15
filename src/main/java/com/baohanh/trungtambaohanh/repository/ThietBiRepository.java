package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.ThietBi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThietBiRepository extends JpaRepository<ThietBi, Integer> {

    // Tìm kiếm thiết bị theo model hoặc hãng sản xuất (không phân biệt chữ hoa/thường)
    List<ThietBi> findByModelContainingIgnoreCaseOrHangSanXuatContainingIgnoreCase(String model, String hangSanXuat);
}