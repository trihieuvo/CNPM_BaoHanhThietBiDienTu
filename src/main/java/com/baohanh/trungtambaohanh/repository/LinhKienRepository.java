package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.LinhKien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface LinhKienRepository extends JpaRepository<LinhKien, Integer> {
    // Tìm kiếm linh kiện theo tên
    List<LinhKien> findByTenLinhKienContainingIgnoreCase(String tenLinhKien);
    
    Page<LinhKien> findByTenLinhKienContainingIgnoreCase(String tenLinhKien, Pageable pageable);
    
    @Query("SELECT lk FROM LinhKien lk WHERE " +
            ":keyword IS NULL OR :keyword = '' OR " +
            "LOWER(lk.tenLinhKien) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(lk.maLinhKien AS string) LIKE CONCAT('%', :keyword, '%')")
     List<LinhKien> searchByKeyword(@Param("keyword") String keyword);
}