package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.ChiTietSuaChua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietSuaChuaRepository extends JpaRepository<ChiTietSuaChua, Integer> {
    
    // Tìm tất cả chi tiết theo mã phiếu
    List<ChiTietSuaChua> findByPhieuSuaChua_MaPhieu(Integer maPhieu);
    
    // Xóa tất cả chi tiết của một phiếu
    void deleteByPhieuSuaChua_MaPhieu(Integer maPhieu);
}