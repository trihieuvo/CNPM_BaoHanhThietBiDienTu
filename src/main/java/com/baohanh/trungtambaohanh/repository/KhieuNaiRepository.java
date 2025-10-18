package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.KhieuNai;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhieuNaiRepository extends JpaRepository<KhieuNai, Integer> {
	List<KhieuNai> findByKhachHang_MaKHOrderByNgayGuiDesc(Integer maKH);
}