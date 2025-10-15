package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.entity.LinhKien;
import com.baohanh.trungtambaohanh.repository.LinhKienRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class LinhKienApiController {

    private final LinhKienRepository linhKienRepository;

    public LinhKienApiController(LinhKienRepository linhKienRepository) {
        this.linhKienRepository = linhKienRepository;
    }

    @GetMapping("/linh-kien")
    public Map<String, Object> getLinhKien(
            @RequestParam(value = "term", required = false, defaultValue = "") String term,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("tenLinhKien"));
        Page<LinhKien> linhKienPage = linhKienRepository.findByTenLinhKienContainingIgnoreCase(term, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("results", linhKienPage.getContent().stream()
                .map(lk -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", lk.getMaLinhKien());
                    
                    // Đơn giản hóa: Hiển thị tất cả các mục theo cùng một định dạng
                    String text = String.format("%s (Tồn: %d)", lk.getTenLinhKien(), lk.getSoLuongTon());
                    
                    map.put("text", text);
                    map.put("data_price", lk.getDonGia());
                    return map;
                })
                .collect(Collectors.toList()));
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("more", linhKienPage.hasNext());
        response.put("pagination", pagination);

        return response;
    }
}