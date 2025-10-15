package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.ThietBi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThietBiRepository extends JpaRepository<ThietBi, Integer> {
    List<ThietBi> findByKhachHang_MaKH(Integer maKH);
    Optional<ThietBi> findBySoSerial(String soSerial);
}