package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.repository.*;
import com.baohanh.trungtambaohanh.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat; // Import này vẫn cần
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/receptionist")
public class ReceptionistController {

    private static final Logger logger = LoggerFactory.getLogger(ReceptionistController.class);

    // ... (Khai báo các repository giữ nguyên) ...
    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final KhachHangRepository khachHangRepository;
    private final ThietBiRepository thietBiRepository;
    private final NhanVienRepository nhanVienRepository;
    private final LoaiThietBiRepository loaiThietBiRepository;
    private final ChiTietSuaChuaRepository chiTietSuaChuaRepository;


    // ... (Constructor giữ nguyên) ...
    public ReceptionistController(PhieuSuaChuaRepository phieuSuaChuaRepository,
                                 KhachHangRepository khachHangRepository,
                                 ThietBiRepository thietBiRepository,
                                 NhanVienRepository nhanVienRepository,
                                 LoaiThietBiRepository loaiThietBiRepository,
                                 ChiTietSuaChuaRepository chiTietSuaChuaRepository ) {
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.khachHangRepository = khachHangRepository;
        this.thietBiRepository = thietBiRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.loaiThietBiRepository = loaiThietBiRepository;
        this.chiTietSuaChuaRepository = chiTietSuaChuaRepository;
    }


    // ... (addReceptionistNameToModel, showDashboard, showTiepNhanForm giữ nguyên) ...
     @ModelAttribute("receptionistName")
     public String addReceptionistNameToModel(Principal principal) {
         if (principal != null) {
             String username = principal.getName();
             return nhanVienRepository.findByTaiKhoan_TenDangNhap(username)
                     .map(NhanVien::getHoTen)
                     .orElse(username);
         }
         return null;
     }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                @RequestParam(name = "size", defaultValue = "10") int size,
                                @RequestParam(name = "keyword", required = false) String keyword,
                                @RequestParam(name = "status", required = false) String status) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("ngayTiepNhan").descending());
        Page<PhieuSuaChua> phieuSuaChuaPage = phieuSuaChuaRepository.searchAndFilter(keyword, status, pageable);

        model.addAttribute("phieuSuaChuaPage", phieuSuaChuaPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentStatus", status);
        return "receptionist/dashboard";
    }

    @GetMapping("/tiep-nhan")
    public String showTiepNhanForm(Model model) {
        // Chỉ cần add các attribute cần thiết cho dropdown và để giữ giá trị nếu lỗi
        model.addAttribute("loaiThietBiList", loaiThietBiRepository.findAll());
        model.addAttribute("technicians", nhanVienRepository.findAllTechnicians());
        // Nếu không có lỗi, các giá trị này sẽ là null/trống
        if (!model.containsAttribute("tinhTrangTiepNhanValue")) {
            model.addAttribute("tinhTrangTiepNhanValue", "");
        }
        // ... thêm các attribute khác nếu cần giữ giá trị ...
        return "receptionist/tiep-nhan";
    }


    @PostMapping("/tiep-nhan/save")
    public String saveTiepNhan( // Bỏ @ModelAttribute PhieuSuaChua phieuSuaChua
                               @RequestParam("tinhTrangTiepNhan") String tinhTrangTiepNhan, // Nhận trực tiếp
                               @RequestParam("soDienThoaiKH") String soDienThoaiKH,
                               @RequestParam("hoTenKH") String hoTenKH,
                               @RequestParam(value = "diaChiKH", required = false) String diaChiKH,
                               @RequestParam(value = "emailKH", required = false) String emailKH,
                               @RequestParam("maLoaiTB") Integer maLoaiTB,
                               @RequestParam("hangSanXuatTB") String hangSanXuatTB,
                               @RequestParam("modelTB") String modelTB,
                               @RequestParam(value = "soSerialTB", required = false) String soSerialTB,
                               @RequestParam(value = "moTaTB", required = false) String moTaTB,
                               @RequestParam("maKyThuatVien") Integer maKyThuatVien,
                               @RequestParam(value = "ngayTraDuKien", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                               LocalDate ngayTraDuKienDate,
                               Principal principal, RedirectAttributes redirectAttributes) {

        PhieuSuaChua phieuSuaChua = new PhieuSuaChua(); // Tạo đối tượng thủ công

        try {
            // Set trường nhận trực tiếp từ @RequestParam
            phieuSuaChua.setTinhTrangTiepNhan(tinhTrangTiepNhan);

            // --- Logic xử lý Khách hàng, Thiết bị, Nhân viên, KTV (giữ nguyên) ---
             // 1. Tìm hoặc tạo khách hàng
             KhachHang khachHang = khachHangRepository.findBySoDienThoai(soDienThoaiKH)
                     .map(existingKh -> { /* ... logic cập nhật KH ... */
                        boolean updated = false;
                        if (!existingKh.getHoTen().equals(hoTenKH)) {
                            existingKh.setHoTen(hoTenKH); updated = true;
                        }
                        if (diaChiKH != null && !diaChiKH.equals(existingKh.getDiaChi())) {
                            existingKh.setDiaChi(diaChiKH); updated = true;
                        }
                        if (emailKH != null && !emailKH.equals(existingKh.getEmail())) {
                            existingKh.setEmail(emailKH); updated = true;
                        }
                        return updated ? khachHangRepository.save(existingKh) : existingKh;
                     })
                     .orElseGet(() -> { /* ... logic tạo KH mới ... */
                         KhachHang newKh = new KhachHang();
                         newKh.setSoDienThoai(soDienThoaiKH);
                         newKh.setHoTen(hoTenKH);
                         newKh.setDiaChi(diaChiKH);
                         newKh.setEmail(emailKH);
                         return khachHangRepository.save(newKh);
                     });

             // 2. Tìm loại thiết bị
             LoaiThietBi loaiThietBi = loaiThietBiRepository.findById(maLoaiTB)
                     .orElseThrow(() -> new RuntimeException("Loại thiết bị không hợp lệ"));

            // 3. Tạo hoặc cập nhật thiết bị
             ThietBi thietBi;
             if (soSerialTB != null && !soSerialTB.trim().isEmpty()) {
                 Optional<ThietBi> existingThietBiOpt = thietBiRepository.findBySoSerial(soSerialTB.trim());
                 if (existingThietBiOpt.isPresent()) {
                     thietBi = existingThietBiOpt.get();
                     if (!thietBi.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
                          logger.warn("Thiết bị Serial {} đang được chuyển từ KH {} sang KH {}", soSerialTB, thietBi.getKhachHang().getMaKH(), khachHang.getMaKH());
                          thietBi.setKhachHang(khachHang);
                     }
                     thietBi.setLoaiThietBi(loaiThietBi);
                     thietBi.setHangSanXuat(hangSanXuatTB);
                     thietBi.setModel(modelTB);
                     thietBi.setMoTa(moTaTB);
                 } else {
                     thietBi = createNewThietBi(khachHang, loaiThietBi, hangSanXuatTB, modelTB, soSerialTB.trim(), moTaTB);
                 }
             } else {
                  thietBi = createNewThietBi(khachHang, loaiThietBi, hangSanXuatTB, modelTB, null, moTaTB);
             }
             thietBi = thietBiRepository.save(thietBi);


            NhanVien nhanVienTiepNhan = nhanVienRepository.findByTaiKhoan_TenDangNhap(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên tiếp nhận"));

            NhanVien kyThuatVien = nhanVienRepository.findById(maKyThuatVien)
                                    .orElseThrow(() -> new RuntimeException("Kỹ thuật viên được chọn không hợp lệ"));

            // --- Set các trường còn lại cho PhieuSuaChua ---
            phieuSuaChua.setKhachHang(khachHang);
            phieuSuaChua.setThietBi(thietBi);
            phieuSuaChua.setNhanVienTiepNhan(nhanVienTiepNhan);
            phieuSuaChua.setKyThuatVien(kyThuatVien);
            phieuSuaChua.setNgayTiepNhan(OffsetDateTime.now());
            phieuSuaChua.setTrangThai("Mới tiếp nhận");

            // --- Xử lý chuyển đổi và gán ngayTraDuKien ---
            if (ngayTraDuKienDate != null) {
                OffsetDateTime ngayTraDuKienOffset = ngayTraDuKienDate.atTime(LocalTime.MIDNIGHT).atOffset(ZoneOffset.UTC);
                phieuSuaChua.setNgayTraDuKien(ngayTraDuKienOffset);
            } else {
                 phieuSuaChua.setNgayTraDuKien(null);
            }

            PhieuSuaChua savedPhieu = phieuSuaChuaRepository.save(phieuSuaChua);

            redirectAttributes.addFlashAttribute("successMessage", "Đã tiếp nhận thành công! Mã phiếu: #" + savedPhieu.getMaPhieu());
            return "redirect:/receptionist/dashboard";

        } catch (Exception e) {
            logger.error("Lỗi nghiêm trọng khi tiếp nhận thiết bị:", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Tiếp nhận thất bại: " + e.getMessage());
            // Thêm lại các giá trị đã nhập vào redirectAttributes
            redirectAttributes.addFlashAttribute("tinhTrangTiepNhanValue", tinhTrangTiepNhan); // <-- Thêm dòng này
            redirectAttributes.addFlashAttribute("soDienThoaiKH", soDienThoaiKH);
            redirectAttributes.addFlashAttribute("hoTenKH", hoTenKH);
            redirectAttributes.addFlashAttribute("diaChiKH", diaChiKH);
            redirectAttributes.addFlashAttribute("emailKH", emailKH);
            redirectAttributes.addFlashAttribute("selectedMaLoaiTB", maLoaiTB);
            redirectAttributes.addFlashAttribute("hangSanXuatTB", hangSanXuatTB);
            redirectAttributes.addFlashAttribute("modelTB", modelTB);
            redirectAttributes.addFlashAttribute("soSerialTB", soSerialTB);
            redirectAttributes.addFlashAttribute("moTaTB", moTaTB);
            redirectAttributes.addFlashAttribute("selectedKtvId", maKyThuatVien);
            if (ngayTraDuKienDate != null) {
               redirectAttributes.addFlashAttribute("ngayTraDuKienValue", ngayTraDuKienDate.toString());
            }
            return "redirect:/receptionist/tiep-nhan";
        }
    }

    // Hàm tiện ích để tạo đối tượng ThietBi mới (giữ nguyên)
    private ThietBi createNewThietBi(KhachHang khachHang, LoaiThietBi loaiThietBi, String hangSX, String model, String serial, String moTa) {
         ThietBi thietBi = new ThietBi();
         thietBi.setKhachHang(khachHang);
         thietBi.setLoaiThietBi(loaiThietBi);
         thietBi.setHangSanXuat(hangSX);
         thietBi.setModel(model);
         thietBi.setSoSerial(serial);
         thietBi.setMoTa(moTa);
         return thietBi;
    }


    // ... (xemChiTietPhieu, traThietBi, findCustomerByPhone giữ nguyên) ...
     @GetMapping("/phieu-sua-chua/{id}")
    public String xemChiTietPhieu(@PathVariable("id") Integer id, Model model) {
         Optional<PhieuSuaChua> phieuOpt = phieuSuaChuaRepository.findById(id);
         if (phieuOpt.isPresent()) {
             PhieuSuaChua phieu = phieuOpt.get();
             List<ChiTietSuaChua> chiTietList = chiTietSuaChuaRepository.findByPhieuSuaChua_MaPhieu(id);
             model.addAttribute("phieu", phieu);
             model.addAttribute("chiTietList", chiTietList);
             return "receptionist/chi-tiet-phieu";
         }
          return "redirect:/receptionist/dashboard";
    }

    @PostMapping("/tra-thiet-bi/{id}")
     public String traThietBi(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
         try {
             PhieuSuaChua phieu = phieuSuaChuaRepository.findById(id)
                     .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu sửa chữa #" + id));

             if (!"Đã sửa xong".equals(phieu.getTrangThai())) {
                  redirectAttributes.addFlashAttribute("errorMessage", "Không thể trả phiếu #" + id + " vì chưa sửa xong (Trạng thái hiện tại: " + phieu.getTrangThai() + ").");
                  return "redirect:/receptionist/phieu-sua-chua/" + id;
             }

             phieu.setTrangThai("Đã trả khách");
             phieuSuaChuaRepository.save(phieu);

             redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận trả thiết bị cho khách hàng (Phiếu #" + id + ").");
             return "redirect:/receptionist/dashboard";

         } catch (Exception e) {
             logger.error("Lỗi khi thực hiện trả thiết bị cho phiếu #{}", id, e);
             redirectAttributes.addFlashAttribute("errorMessage", "Trả thiết bị thất bại: " + e.getMessage());
             return "redirect:/receptionist/phieu-sua-chua/" + id;
         }
     }

     @GetMapping("/api/customers/find")
     @ResponseBody
     public ResponseEntity<?> findCustomerByPhone(@RequestParam("phone") String phone) {
        Optional<KhachHang> khachHangOpt = khachHangRepository.findBySoDienThoai(phone);
        if (khachHangOpt.isPresent()) {
            return ResponseEntity.ok(khachHangOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
     }
}