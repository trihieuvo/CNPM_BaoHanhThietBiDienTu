package com.baohanh.trungtambaohanh.repository;

import com.baohanh.trungtambaohanh.entity.LoaiThietBi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Annotation này sẽ cấu hình môi trường test chỉ cho tầng JPA
public class LoaiThietBiRepositoryTest {

    @Autowired
    private LoaiThietBiRepository loaiThietBiRepository;

    @Test
    void whenSaveAndFindById_thenCorrect() {
        // 1. Arrange: Chuẩn bị dữ liệu
        LoaiThietBi loaiThietBiMoi = new LoaiThietBi(null, "Laptop Gaming");

        // 2. Act: Thực hiện hành động (lưu vào database)
        LoaiThietBi daLuu = loaiThietBiRepository.save(loaiThietBiMoi);
        LoaiThietBi timThay = loaiThietBiRepository.findById(daLuu.getMaLoaiTB()).orElse(null);

        // 3. Assert: Kiểm tra kết quả
        assertThat(daLuu).isNotNull();
        assertThat(daLuu.getMaLoaiTB()).isGreaterThan(0);

        assertThat(timThay).isNotNull();
        assertThat(timThay.getTenLoaiTB()).isEqualTo("Laptop Gaming");

        System.out.println("Test thành công! Đã lưu và tìm thấy: " + timThay.getTenLoaiTB());
    }
}