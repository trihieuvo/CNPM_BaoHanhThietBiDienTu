package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.repository.*;
import com.baohanh.trungtambaohanh.entity.*;
import com.baohanh.trungtambaohanh.service.InvoiceService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final KhachHangRepository khachHangRepository;
    private final ThietBiRepository thietBiRepository;
    private final NhanVienRepository nhanVienRepository;
    private final LoaiThietBiRepository loaiThietBiRepository;
    private final ChiTietSuaChuaRepository chiTietSuaChuaRepository;
    private final InvoiceService invoiceService;
    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final PasswordEncoder passwordEncoder;

    public ReceptionistController(PhieuSuaChuaRepository phieuSuaChuaRepository,
                                 KhachHangRepository khachHangRepository,
                                 ThietBiRepository thietBiRepository,
                                 NhanVienRepository nhanVienRepository,
                                 LoaiThietBiRepository loaiThietBiRepository,
                                 ChiTietSuaChuaRepository chiTietSuaChuaRepository,
                                 InvoiceService invoiceService,
                                 TaiKhoanRepository taiKhoanRepository,
                                 VaiTroRepository vaiTroRepository,
                                 PasswordEncoder passwordEncoder) {
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.khachHangRepository = khachHangRepository;
        this.thietBiRepository = thietBiRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.loaiThietBiRepository = loaiThietBiRepository;
        this.chiTietSuaChuaRepository = chiTietSuaChuaRepository;
        this.invoiceService = invoiceService;
        this.taiKhoanRepository = taiKhoanRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
        model.addAttribute("loaiThietBiList", loaiThietBiRepository.findAll());
        model.addAttribute("technicians", nhanVienRepository.findAllTechnicians());
        if (!model.containsAttribute("tinhTrangTiepNhanValue")) {
            model.addAttribute("tinhTrangTiepNhanValue", "");
        }
        // Thêm các attribute khác nếu cần giữ giá trị
         if (!model.containsAttribute("soDienThoaiKH")) model.addAttribute("soDienThoaiKH", "");
         if (!model.containsAttribute("hoTenKH")) model.addAttribute("hoTenKH", "");
         if (!model.containsAttribute("diaChiKH")) model.addAttribute("diaChiKH", "");
         if (!model.containsAttribute("emailKH")) model.addAttribute("emailKH", "");
         if (!model.containsAttribute("selectedMaLoaiTB")) model.addAttribute("selectedMaLoaiTB", null);
         if (!model.containsAttribute("hangSanXuatTB")) model.addAttribute("hangSanXuatTB", "");
         if (!model.containsAttribute("modelTB")) model.addAttribute("modelTB", "");
         if (!model.containsAttribute("soSerialTB")) model.addAttribute("soSerialTB", "");
         if (!model.containsAttribute("moTaTB")) model.addAttribute("moTaTB", "");
         if (!model.containsAttribute("selectedKtvId")) model.addAttribute("selectedKtvId", null);
         if (!model.containsAttribute("ngayTraDuKienValue")) model.addAttribute("ngayTraDuKienValue", "");

        return "receptionist/tiep-nhan";
    }


    @PostMapping("/tiep-nhan/save")
    public String saveTiepNhan(
                               @RequestParam("tinhTrangTiepNhan") String tinhTrangTiepNhan,
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

        PhieuSuaChua phieuSuaChua = new PhieuSuaChua();

        try {
            phieuSuaChua.setTinhTrangTiepNhan(tinhTrangTiepNhan);

             KhachHang khachHang = khachHangRepository.findBySoDienThoai(soDienThoaiKH)
                     .map(existingKh -> {
                        boolean updated = false;
                        if (hoTenKH != null && !hoTenKH.isEmpty() && !hoTenKH.equals(existingKh.getHoTen())) {
                            existingKh.setHoTen(hoTenKH); updated = true;
                        }
                        if (diaChiKH != null && !diaChiKH.equals(existingKh.getDiaChi())) {
                            existingKh.setDiaChi(diaChiKH); updated = true;
                        }
                         if (emailKH != null && !emailKH.isEmpty() && !emailKH.equals(existingKh.getEmail())) {
                             existingKh.setEmail(emailKH); updated = true;
                         }
                        return updated ? khachHangRepository.save(existingKh) : existingKh;
                     })
                     .orElseGet(() -> {
                         if (hoTenKH == null || hoTenKH.trim().isEmpty()) {
                             throw new IllegalArgumentException("Họ tên khách hàng là bắt buộc khi tạo mới.");
                         }
                         KhachHang newKh = new KhachHang();
                         newKh.setSoDienThoai(soDienThoaiKH);
                         newKh.setHoTen(hoTenKH);
                         newKh.setDiaChi(diaChiKH);
                         newKh.setEmail(emailKH != null && !emailKH.isEmpty() ? emailKH : null); // Chỉ lưu email nếu có

                         // Tạo tài khoản mới cho khách hàng
                         TaiKhoan taiKhoan = new TaiKhoan();
                         taiKhoan.setTenDangNhap(soDienThoaiKH);
                         taiKhoan.setMatKhauHash(passwordEncoder.encode("123456"));
                         
                         // === BẮT ĐẦU THAY ĐỔI ===
                         taiKhoan.setTrangThai(true); // Kích hoạt tài khoản
                         taiKhoan.setNgayTao(OffsetDateTime.now()); // Đặt ngày tạo
                         // === KẾT THÚC THAY ĐỔI ===

                         VaiTro customerRole = vaiTroRepository.findByTenVaiTro("Khách hàng")
                                 .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy vai trò Khách hàng"));
                         taiKhoan.setVaiTro(customerRole);
                         taiKhoanRepository.save(taiKhoan);

                         newKh.setTaiKhoan(taiKhoan);
                         return khachHangRepository.save(newKh);
                     });

             LoaiThietBi loaiThietBi = loaiThietBiRepository.findById(maLoaiTB)
                     .orElseThrow(() -> new RuntimeException("Loại thiết bị không hợp lệ"));

             ThietBi thietBi;
             if (soSerialTB != null && !soSerialTB.trim().isEmpty()) {
                 Optional<ThietBi> existingThietBiOpt = thietBiRepository.findBySoSerial(soSerialTB.trim());
                 if (existingThietBiOpt.isPresent()) {
                     thietBi = existingThietBiOpt.get();
                     // Chỉ cập nhật KH nếu serial thuộc KH khác VÀ KH mới đã tồn tại (có MaKH)
                      if (khachHang.getMaKH() != null && !thietBi.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
                           logger.warn("Thiết bị Serial {} đang được chuyển từ KH {} sang KH {}", soSerialTB, thietBi.getKhachHang().getMaKH(), khachHang.getMaKH());
                           thietBi.setKhachHang(khachHang); // Cập nhật chủ sở hữu
                      }
                     // Luôn cập nhật các thông tin khác
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

            phieuSuaChua.setKhachHang(khachHang);
            phieuSuaChua.setThietBi(thietBi);
            phieuSuaChua.setNhanVienTiepNhan(nhanVienTiepNhan);
            phieuSuaChua.setKyThuatVien(kyThuatVien);
            phieuSuaChua.setNgayTiepNhan(OffsetDateTime.now());
            phieuSuaChua.setTrangThai("Mới tiếp nhận");

            if (ngayTraDuKienDate != null) {
                OffsetDateTime ngayTraDuKienOffset = ngayTraDuKienDate.atTime(LocalTime.MIDNIGHT).atOffset(ZoneOffset.UTC); // Hoặc ZoneOffset.ofHours(7) nếu muốn giờ VN
                phieuSuaChua.setNgayTraDuKien(ngayTraDuKienOffset);
            } else {
                 phieuSuaChua.setNgayTraDuKien(null);
            }

            PhieuSuaChua savedPhieu = phieuSuaChuaRepository.save(phieuSuaChua);

            redirectAttributes.addFlashAttribute("successMessage", "Đã tiếp nhận thành công! Mã phiếu: #" + savedPhieu.getMaPhieu());
            return "redirect:/receptionist/dashboard";

        } catch (IllegalArgumentException e) { // Bắt lỗi cụ thể
             logger.warn("Lỗi nhập liệu khi tiếp nhận: {}", e.getMessage());
             redirectAttributes.addFlashAttribute("errorMessage", "Tiếp nhận thất bại: " + e.getMessage());
             // Giữ lại giá trị đã nhập
             addFormDataToRedirectAttributes(redirectAttributes, tinhTrangTiepNhan, soDienThoaiKH, hoTenKH, diaChiKH, emailKH, maLoaiTB, hangSanXuatTB, modelTB, soSerialTB, moTaTB, maKyThuatVien, ngayTraDuKienDate);
             return "redirect:/receptionist/tiep-nhan";
        } catch (Exception e) {
            logger.error("Lỗi hệ thống khi tiếp nhận thiết bị:", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Tiếp nhận thất bại do lỗi hệ thống. Vui lòng thử lại hoặc liên hệ quản trị viên.");
            // Giữ lại giá trị đã nhập
            addFormDataToRedirectAttributes(redirectAttributes, tinhTrangTiepNhan, soDienThoaiKH, hoTenKH, diaChiKH, emailKH, maLoaiTB, hangSanXuatTB, modelTB, soSerialTB, moTaTB, maKyThuatVien, ngayTraDuKienDate);
            return "redirect:/receptionist/tiep-nhan";
        }
    }
     // Helper method để thêm dữ liệu form vào RedirectAttributes
     private void addFormDataToRedirectAttributes(RedirectAttributes redirectAttributes, String tinhTrangTiepNhan, String soDienThoaiKH, String hoTenKH, String diaChiKH, String emailKH, Integer maLoaiTB, String hangSanXuatTB, String modelTB, String soSerialTB, String moTaTB, Integer maKyThuatVien, LocalDate ngayTraDuKienDate) {
         redirectAttributes.addFlashAttribute("tinhTrangTiepNhanValue", tinhTrangTiepNhan);
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
     }


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

     @GetMapping("/phieu-sua-chua/{id}")
    public String xemChiTietPhieu(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) { // Thêm RedirectAttributes
         Optional<PhieuSuaChua> phieuOpt = phieuSuaChuaRepository.findById(id);
         if (phieuOpt.isPresent()) {
             PhieuSuaChua phieu = phieuOpt.get();
             // Không cần lấy chi tiết ở đây nữa vì Entity PhieuSuaChua đã có @OneToMany
             // List<ChiTietSuaChua> chiTietList = chiTietSuaChuaRepository.findByPhieuSuaChua_MaPhieu(id);
             model.addAttribute("phieu", phieu);
             // model.addAttribute("chiTietList", chiTietList); // Bỏ dòng này
             return "receptionist/chi-tiet-phieu";
         }
         // Nếu không tìm thấy phiếu, redirect về dashboard với thông báo lỗi
         redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phiếu sửa chữa #" + id);
          return "redirect:/receptionist/dashboard";
    }

    @PostMapping("/tra-thiet-bi/{id}")
     public String traThietBi(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
         try {
             PhieuSuaChua phieu = phieuSuaChuaRepository.findById(id)
                     .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu sửa chữa #" + id));

             // Cho phép trả cả khi "Đã sửa xong" hoặc "Trả về - không sửa được"
              List<String> validStates = List.of("Đã sửa xong", "Trả về - không sửa được");
              if (!validStates.contains(phieu.getTrangThai())) {
                   redirectAttributes.addFlashAttribute("errorMessage", "Không thể trả phiếu #" + id + " vì chưa xử lý xong (Trạng thái: " + phieu.getTrangThai() + ").");
                   return "redirect:/receptionist/phieu-sua-chua/" + id;
              }

             phieu.setTrangThai("Đã trả khách");
             // Cập nhật ngày hoàn thành nếu chưa có (trường hợp trả máy không sửa được)
             if (phieu.getNgayHoanThanh() == null) {
                 phieu.setNgayHoanThanh(OffsetDateTime.now());
             }
             phieuSuaChuaRepository.save(phieu);

             redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận trả thiết bị cho khách hàng (Phiếu #" + id + ").");
             return "redirect:/receptionist/dashboard";

         } catch (Exception e) {
             logger.error("Lỗi khi thực hiện trả thiết bị cho phiếu #{}", id, e);
             redirectAttributes.addFlashAttribute("errorMessage", "Trả thiết bị thất bại: " + e.getMessage());
             // Redirect về trang chi tiết thay vì dashboard để người dùng thấy lỗi
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
            // Trả về đối tượng trống thay vì 404 để JS dễ xử lý
             return ResponseEntity.ok(new KhachHang()); // Hoặc ResponseEntity.noContent().build();
        }
     }

     // --- PHƯƠNG THỨC XUẤT HÓA ĐƠN PDF ---
     @GetMapping("/phieu-sua-chua/{id}/invoice")
     public ResponseEntity<InputStreamResource> exportInvoicePdf(@PathVariable("id") Integer id) {
         try {
             PhieuSuaChua phieu = phieuSuaChuaRepository.findById(id)
                     .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu sửa chữa #" + id));

             // Có thể thêm kiểm tra trạng thái ở đây nếu chỉ muốn xuất hóa đơn sau khi đã trả khách
             // if (!"Đã trả khách".equals(phieu.getTrangThai())) {
             //    throw new RuntimeException("Chỉ có thể xuất hóa đơn cho phiếu đã trả khách.");
             // }

             ByteArrayInputStream bis = invoiceService.createInvoicePdf(phieu);

             HttpHeaders headers = new HttpHeaders();
             String rawFileName = "HoaDon_Phieu_" + id + ".pdf";
             // Mã hóa tên file để hỗ trợ tiếng Việt và ký tự đặc biệt
             String encodedFileName = URLEncoder.encode(rawFileName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
             // inline: mở trong trình duyệt, attachment: tải về
             headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedFileName);

             return ResponseEntity
                     .ok()
                     .headers(headers)
                     .contentType(MediaType.APPLICATION_PDF)
                     .body(new InputStreamResource(bis));

         } catch (Exception e) {
             logger.error("Lỗi khi xuất hóa đơn PDF cho phiếu #{}", id, e);
             // Trả về lỗi 500 hoặc trang lỗi tùy chỉnh
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .contentType(MediaType.TEXT_PLAIN) // Hoặc application/json
                                 .body(null); // Không trả về InputStreamResource khi có lỗi
         }
     }
}