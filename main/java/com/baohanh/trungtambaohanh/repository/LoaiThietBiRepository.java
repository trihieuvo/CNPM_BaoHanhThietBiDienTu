package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.LoaiThietBi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoaiThietBiRepository extends JpaRepository<LoaiThietBi, Integer> {
}