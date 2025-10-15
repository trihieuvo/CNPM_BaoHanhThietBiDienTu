package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.entity.KhachHang;
import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import com.baohanh.trungtambaohanh.entity.ThietBi;
import com.baohanh.trungtambaohanh.repository.KhachHangRepository;
import com.baohanh.trungtambaohanh.repository.PhieuSuaChuaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@Controller
@RequestMapping("/receptionist/tiep-nhan")
public class TiepNhanController {

    @Autowired
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    // Hiển thị form để tạo phiếu mới
    @GetMapping
    public String showTiepNhanForm(Model model) {
        model.addAttribute("phieuSuaChua", new PhieuSuaChua());
        model.addAttribute("pageTitle", "Tiếp nhận bảo hành");
        model.addAttribute("content", "~{receptionist/tiep-nhan-form :: content}");
        return "receptionist/receptionist-layout";
    }

    /**
     * API endpoint để tìm kiếm khách hàng bằng SĐT (dùng cho AJAX)
     * @param sdt Số điện thoại để tìm kiếm
     * @return JSON chứa danh sách khách hàng tìm thấy
     */
    @GetMapping("/api/customers/search")
    @ResponseBody
    public ResponseEntity<List<KhachHang>> searchCustomers(@RequestParam("sdt") String sdt) {
        List<KhachHang> customers = khachHangRepository.findBySoDienThoaiContainingIgnoreCase(sdt);
        return ResponseEntity.ok(customers);
    }

    // Xử lý việc submit form
    @PostMapping
    public String processTiepNhanForm(@RequestParam("khachHangId") Integer khachHangId,
                                      @RequestParam("hoTen") String hoTen,
                                      @RequestParam("soDienThoai") String soDienThoai,
                                      @RequestParam("email") String email,
                                      @RequestParam("diaChi") String diaChi,
                                      @ModelAttribute ThietBi thietBi,
                                      @ModelAttribute PhieuSuaChua phieuSuaChua) {

        KhachHang khachHang;

        // Nếu khachHangId > 0, nghĩa là NVTN đã chọn một khách hàng có sẵn.
        if (khachHangId != null && khachHangId > 0) {
            khachHang = khachHangRepository.findById(khachHangId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid customer Id:" + khachHangId));
        } else {
            // Ngược lại, đây là khách hàng mới, tiến hành tạo mới.
            // Kiểm tra xem SĐT đã tồn tại chưa để tránh trùng lặp
            khachHang = khachHangRepository.findBySoDienThoai(soDienThoai)
                    .orElseGet(() -> {
                        KhachHang newCustomer = new KhachHang();
                        newCustomer.setHoTen(hoTen);
                        newCustomer.setSoDienThoai(soDienThoai);
                        newCustomer.setEmail(email);
                        newCustomer.setDiaChi(diaChi);
                        return khachHangRepository.save(newCustomer);
                    });
        }

        thietBi.setKhachHang(khachHang);
        // TODO: Cần logic để lưu thiết bị vào CSDL nếu nó là thiết bị mới.
        // Hiện tại đang giả định ThietBi được tạo cùng PhieuSuaChua

        phieuSuaChua.setKhachHang(khachHang);
        phieuSuaChua.setThietBi(thietBi); // Cần đảm bảo thiết bị đã được lưu
        phieuSuaChua.setNgayTiepNhan(OffsetDateTime.now());
        phieuSuaChua.setTrangThai("Mới tiếp nhận");
        // TODO: Gán nhân viên tiếp nhận đang đăng nhập

        phieuSuaChuaRepository.save(phieuSuaChua);

        // Chuyển hướng đến trang chi tiết phiếu vừa tạo (sẽ làm ở bước sau)
        return "redirect:/receptionist/dashboard";
    }
}