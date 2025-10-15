package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.dto.ChartDataDto;
import com.baohanh.trungtambaohanh.dto.DashboardStatsDto;
import com.baohanh.trungtambaohanh.dto.LinhKienDetailDto;
import com.baohanh.trungtambaohanh.dto.NhanVienDto;
import com.baohanh.trungtambaohanh.dto.PhieuSuaChuaDetailDto;
import com.baohanh.trungtambaohanh.dto.TicketStatsDto; 
import com.baohanh.trungtambaohanh.entity.LinhKien;
import com.baohanh.trungtambaohanh.entity.LoaiThietBi;
import com.baohanh.trungtambaohanh.entity.NhanVien;
import com.baohanh.trungtambaohanh.entity.PhieuSuaChua; 
import com.baohanh.trungtambaohanh.entity.TaiKhoan;
import com.baohanh.trungtambaohanh.repository.LinhKienRepository;
import com.baohanh.trungtambaohanh.repository.LoaiThietBiRepository;
import com.baohanh.trungtambaohanh.repository.NhanVienRepository;
import com.baohanh.trungtambaohanh.repository.PhieuSuaChuaRepository;
import com.baohanh.trungtambaohanh.repository.TaiKhoanRepository;
import com.baohanh.trungtambaohanh.repository.VaiTroRepository;
import com.baohanh.trungtambaohanh.service.BaoCaoService;
import com.baohanh.trungtambaohanh.service.DashboardService;
import com.baohanh.trungtambaohanh.service.ExcelExportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import com.baohanh.trungtambaohanh.service.TaiKhoanService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.baohanh.trungtambaohanh.dto.ChartDataDto; 

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    private final DashboardService dashboardService;
    private final LoaiThietBiRepository loaiThietBiRepository;
    private final LinhKienRepository linhKienRepository;
    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final NhanVienRepository nhanVienRepository;
    private final VaiTroRepository vaiTroRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final TaiKhoanService taiKhoanService;
    private final BaoCaoService baoCaoService;
    private final ExcelExportService excelExportService;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public ManagerController(DashboardService dashboardService,
                             LoaiThietBiRepository loaiThietBiRepository,
                             LinhKienRepository linhKienRepository,
                             PhieuSuaChuaRepository phieuSuaChuaRepository,
                             NhanVienRepository nhanVienRepository,
                             VaiTroRepository vaiTroRepository, 
                             TaiKhoanRepository taiKhoanRepository, 
                             TaiKhoanService taiKhoanService,
                             BaoCaoService baoCaoService,
                             ExcelExportService excelExportService,
                             PasswordEncoder passwordEncoder) {
        this.dashboardService = dashboardService;
        this.loaiThietBiRepository = loaiThietBiRepository;
        this.linhKienRepository = linhKienRepository;
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.vaiTroRepository = vaiTroRepository; 
        this.taiKhoanRepository = taiKhoanRepository; 
        this.taiKhoanService = taiKhoanService; 
        this.baoCaoService = baoCaoService;
        this.excelExportService = excelExportService;
        this.passwordEncoder = passwordEncoder;
    }
    
    
    @ModelAttribute("managerName")
    public String addManagerNameToModel() {
        // Lấy đối tượng Authentication từ context bảo mật
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Kiểm tra xem người dùng đã được xác thực hay chưa
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName(); // Lấy tên đăng nhập
            
            // Tìm nhân viên tương ứng và trả về tên đầy đủ, hoặc username nếu không thấy
            return nhanVienRepository.findByTaiKhoan_TenDangNhap(username)
                    .map(NhanVien::getHoTen)
                    .orElse(username);
        }
        return null; // Trả về null nếu không có ai đăng nhập
    }
    
    // Trang chủ của Manager, có thể redirect đến dashboard
    @GetMapping
    public String showManagerHome() {
        return "redirect:/manager/dashboard";
    }

    // Trang Dashboard thống kê
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Dữ liệu cho KPIs
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);

        // Dữ liệu cho biểu đồ trạng thái phiếu sửa
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime aMonthAgo = now.minusMonths(1);
        ChartDataDto statusChartData = baoCaoService.getPhieuTheoTrangThaiChart(aMonthAgo, now, null, null);
        model.addAttribute("statusChartData", statusChartData);

        return "manager/dashboard";
    }

    // --- QUẢN LÝ DANH MỤC LOẠI THIẾT BỊ ---
    @GetMapping("/devices")
    public String showDeviceTypesPage(Model model) {
        model.addAttribute("loaiThietBiList", loaiThietBiRepository.findAll());
        if (!model.containsAttribute("loaiThietBi")) {
            model.addAttribute("loaiThietBi", new LoaiThietBi());
        }
        return "manager/devices";
    }

    @PostMapping("/devices/save")
    public String saveDeviceType(@ModelAttribute("loaiThietBi") LoaiThietBi loaiThietBi, RedirectAttributes redirectAttributes) {
        try {
            String successMessage = (loaiThietBi.getMaLoaiTB() == null) ? "Thêm mới loại thiết bị thành công!" : "Cập nhật loại thiết bị thành công!";
            loaiThietBiRepository.save(loaiThietBi);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi lưu.");
        }
        return "redirect:/manager/devices";
    }

    @GetMapping("/devices/delete/{id}")
    public String deleteDeviceType(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            loaiThietBiRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa loại thiết bị thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa. Loại thiết bị này có thể đang được sử dụng.");
        }
        return "redirect:/manager/devices";
    }

 
    @GetMapping("/tickets")
    public String showTicketsPage(@RequestParam(value = "keyword", required = false) String keyword,
                                  @RequestParam(value = "status", required = false) String status,
                                  Model model, HttpServletRequest request) { // Thêm HttpServletRequest
        List<PhieuSuaChua> filteredTickets = phieuSuaChuaRepository.searchAndFilter(keyword, status);

        long moiTiepNhan = phieuSuaChuaRepository.countByTrangThai("Mới tiếp nhận");
        long dangSuaChua = phieuSuaChuaRepository.countByTrangThai("Đang sửa chữa");
        long choLinhKien = phieuSuaChuaRepository.countByTrangThai("Chờ linh kiện");
        long daHoanThanh = phieuSuaChuaRepository.countByTrangThai("Đã sửa xong") + phieuSuaChuaRepository.countByTrangThai("Đã trả khách");
        long treHen = 0;
        TicketStatsDto ticketStats = new TicketStatsDto(moiTiepNhan, dangSuaChua, choLinhKien, daHoanThanh, treHen);

        model.addAttribute("phieuSuaChuaList", filteredTickets);
        model.addAttribute("ticketStats", ticketStats);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentStatus", status);

        // KIỂM TRA AJAX REQUEST
        String requestedWithHeader = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWithHeader)) {
            return "manager/tickets :: results-fragment"; // Trả về fragment
        }

        return "manager/tickets"; 
    }
 


    @GetMapping("/parts")
    public String showPartsPage(@RequestParam(value = "keyword", required = false) String keyword, Model model, HttpServletRequest request) { // Thêm HttpServletRequest
        List<LinhKien> linhKienList;
        if (keyword != null && !keyword.isEmpty()) {
            linhKienList = linhKienRepository.searchByKeyword(keyword);
        } else {
            linhKienList = linhKienRepository.findAll();
        }
        
        model.addAttribute("linhKienList", linhKienList);
        model.addAttribute("keyword", keyword);
        
        if (!model.containsAttribute("linhKien")) {
            model.addAttribute("linhKien", new LinhKien());
        }
        model.addAttribute("loaiThietBiList", loaiThietBiRepository.findAll());

        // KIỂM TRA AJAX REQUEST
        String requestedWithHeader = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWithHeader)) {
            return "manager/parts :: results-fragment"; // Trả về fragment
        }
        
        return "manager/parts";
    }
    
    @PostMapping("/parts/save")
    public String savePart(@ModelAttribute("linhKien") LinhKien linhKien, RedirectAttributes redirectAttributes) {
        try {
            String message = (linhKien.getMaLinhKien() == null) ? "Thêm mới linh kiện thành công!" : "Cập nhật linh kiện thành công!";
            linhKienRepository.save(linhKien);
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            // Bắt lỗi nếu tên/mã linh kiện đã tồn tại (unique constraint)
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Tên hoặc mã linh kiện có thể đã tồn tại.");
            redirectAttributes.addFlashAttribute("linhKien", linhKien); // Trả lại dữ liệu đã nhập
        }
        return "redirect:/manager/parts";
    }
    @GetMapping("/parts/delete/{id}")
    public String deletePart(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            linhKienRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa linh kiện thành công!");
        } catch (Exception e) {
            // Bắt lỗi nếu linh kiện đã được sử dụng trong phiếu sửa chữa
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa linh kiện này vì đã được sử dụng trong phiếu sửa chữa.");
        }
        return "redirect:/manager/parts";
    }
    
    
    @GetMapping("/employees")
	public String showEmployeesPage(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "vaiTroId", required = false) Integer vaiTroId, Model model, HttpServletRequest request) { // Thêm HttpServletRequest
		List<NhanVien> nhanVienList = nhanVienRepository.searchAndFilter(keyword, vaiTroId);

		model.addAttribute("nhanVienList", nhanVienList);
		model.addAttribute("vaiTroList", vaiTroRepository.findAll());
		model.addAttribute("keyword", keyword);
		model.addAttribute("currentVaiTroId", vaiTroId);

		if (!model.containsAttribute("nhanVienDto")) {
			model.addAttribute("nhanVienDto", new NhanVienDto());
		}

        // KIỂM TRA AJAX REQUEST
        String requestedWithHeader = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWithHeader)) {
            return "manager/employees :: results-fragment"; // Trả về fragment
        }

		return "manager/employees";
    }
    @PostMapping("/employees/save")
    public String saveEmployee(@ModelAttribute("nhanVienDto") NhanVienDto nhanVienDto,
                             RedirectAttributes redirectAttributes) {
        try {
            taiKhoanService.saveNhanVien(nhanVienDto);
            String message = (nhanVienDto.getMaNV() == null) ? "Thêm mới nhân viên thành công!" : "Cập nhật thông tin thành công!";
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("nhanVienDto", nhanVienDto); // Giữ lại dữ liệu đã nhập
        }
        return "redirect:/manager/employees";
    }

    @GetMapping("/employees/toggle-status/{id}")
    public String toggleEmployeeStatus(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        Optional<NhanVien> nhanVienOpt = nhanVienRepository.findById(id);
        if (nhanVienOpt.isPresent()) {
            TaiKhoan taiKhoan = nhanVienOpt.get().getTaiKhoan();
            taiKhoan.setTrangThai(!taiKhoan.isTrangThai()); // Đảo ngược trạng thái
            taiKhoanRepository.save(taiKhoan);
            String status = taiKhoan.isTrangThai() ? "Mở khóa" : "Khóa";
            redirectAttributes.addFlashAttribute("successMessage", status + " tài khoản thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhân viên.");
        }
        return "redirect:/manager/employees";
    }
    
    
    
    @GetMapping("/tickets/details/{id}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTicketDetails(@PathVariable("id") Integer id) {
        Optional<PhieuSuaChua> phieuSuaChuaOpt = phieuSuaChuaRepository.findById(id);

        if (phieuSuaChuaOpt.isPresent()) {
            PhieuSuaChua phieu = phieuSuaChuaOpt.get();
            
            // --- Ánh xạ từ Entity sang DTO để tránh lỗi tham chiếu vòng tròn ---
            PhieuSuaChuaDetailDto dto = new PhieuSuaChuaDetailDto();
            dto.setMaPhieu(phieu.getMaPhieu());
            dto.setTrangThai(phieu.getTrangThai());
            dto.setTinhTrangTiepNhan(phieu.getTinhTrangTiepNhan());
            dto.setNgayTiepNhan(phieu.getNgayTiepNhan());
            dto.setTongChiPhi(phieu.getTongChiPhi());

            if (phieu.getKhachHang() != null) {
                dto.setKhachHangHoTen(phieu.getKhachHang().getHoTen());
                dto.setKhachHangSdt(phieu.getKhachHang().getSoDienThoai());
            }

            if (phieu.getThietBi() != null) {
                dto.setThietBiHangSanXuat(phieu.getThietBi().getHangSanXuat());
                dto.setThietBiModel(phieu.getThietBi().getModel());
                dto.setThietBiSoSerial(phieu.getThietBi().getSoSerial());
            }

            if (phieu.getKyThuatVien() != null) {
                dto.setKtvHoTen(phieu.getKyThuatVien().getHoTen());
            }

            // Ánh xạ danh sách linh kiện
            List<LinhKienDetailDto> linhKienDtos = phieu.getChiTietSuaChuaList().stream().map(chiTiet -> {
                LinhKienDetailDto lkDto = new LinhKienDetailDto();
                if (chiTiet.getLinhKien() != null) {
                    lkDto.setTenLinhKien(chiTiet.getLinhKien().getTenLinhKien());
                }
                lkDto.setSoLuong(chiTiet.getSoLuong());
                lkDto.setDonGia(chiTiet.getDonGia());
                lkDto.setThanhTien(chiTiet.getThanhTien());
                return lkDto;
            }).collect(Collectors.toList());
            dto.setChiTietSuaChuaList(linhKienDtos);

            return ResponseEntity.ok(dto);
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/revenue-report")
    public String showRevenueReportPage(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "ktvId", required = false) Integer ktvId,
            Model model
            ) {

        // Mặc định là tháng này nếu không có ngày nào được chọn
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        // Chuyển đổi sang OffsetDateTime để truy vấn
        OffsetDateTime startDateTime = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endDateTime = endDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        // Lấy dữ liệu từ service
        List<PhieuSuaChua> phieuList = baoCaoService.getPhieuSuaChuaHoanThanh(startDateTime, endDateTime, ktvId);
      
        
        // Đưa dữ liệu vào model
        model.addAttribute("kpis", baoCaoService.tinhToanKpis(phieuList, startDateTime, endDateTime));
        model.addAttribute("revenueByDayChart", baoCaoService.getDoanhThuTheoNgayChart(startDateTime, endDateTime, ktvId));
        model.addAttribute("revenueByDeviceTypeChart", baoCaoService.getDoanhThuTheoLoaiThietBiChart(startDateTime, endDateTime, ktvId));
        model.addAttribute("detailedTickets", phieuList);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("technicians", nhanVienRepository.findAllTechnicians()); 
        model.addAttribute("currentKtvId", ktvId); // Giữ lại giá trị đã lọc


        return "manager/revenue-report";
    }
    
    @GetMapping("/revenue-report/export")
    public ResponseEntity<InputStreamResource> exportRevenueReportToExcel(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "ktvId", required = false) Integer ktvId) {
        
        // --- Logic lấy dữ liệu (giữ nguyên như cũ) ---
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();
        OffsetDateTime startDateTime = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endDateTime = endDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
        List<PhieuSuaChua> phieuList = baoCaoService.getPhieuSuaChuaHoanThanh(startDateTime, endDateTime, ktvId);

        // --- Tạo tên file động và mã hóa nó ---
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String ktvName = "Tat_Ca_KTV"; // Dùng gạch dưới cho an toàn
        if (ktvId != null) {
            Optional<NhanVien> nv = nhanVienRepository.findById(ktvId);
            if (nv.isPresent()) {
                // Thay thế khoảng trắng bằng gạch dưới
                ktvName = nv.get().getHoTen().replaceAll("\\s+", "_"); 
            }
        }
        String rawFileName = String.format("BaoCaoDoanhThu_%s_Tu_%s_Den_%s.xlsx", 
                                        ktvName, 
                                        startDate.format(formatter), 
                                        endDate.format(formatter));
        
        // Mã hóa tên file để hỗ trợ ký tự Unicode (tiếng Việt)
        String encodedFileName = URLEncoder.encode(rawFileName, StandardCharsets.UTF_8).replace("+", "%20");

        // --- Gọi service và trả về file với header đã được sửa lỗi ---
        ByteArrayInputStream in = excelExportService.createRevenueReportExcel(phieuList);

        HttpHeaders headers = new HttpHeaders();
        // Cú pháp `filename*=` đảm bảo trình duyệt hiểu đúng tên file đã được mã hóa
        headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }
    
    @GetMapping("/repair-stats")
    public String showRepairStatsPage(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "ktvId", required = false) Integer ktvId,
            @RequestParam(value = "loaiThietBiId", required = false) Integer loaiThietBiId,
            Model model) {

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        OffsetDateTime startDateTime = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endDateTime = endDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        model.addAttribute("kpis", baoCaoService.tinhToanThongKeSuaChuaKpis(startDateTime, endDateTime, ktvId, loaiThietBiId));
        model.addAttribute("statusChart", baoCaoService.getPhieuTheoTrangThaiChart(startDateTime, endDateTime, ktvId, loaiThietBiId));
        model.addAttribute("ktvChart", baoCaoService.getPhieuTheoKtvChart(startDateTime, endDateTime, loaiThietBiId));
        model.addAttribute("linhKienStats", baoCaoService.getThongKeSuDungLinhKien(startDateTime, endDateTime, ktvId, loaiThietBiId));
        
        // Dữ liệu cho bộ lọc
        model.addAttribute("technicians", nhanVienRepository.findAllTechnicians());
        model.addAttribute("deviceTypes", loaiThietBiRepository.findAll());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentKtvId", ktvId);
        model.addAttribute("currentDeviceTypeId", loaiThietBiId);
        
        return "manager/repair-stats";
    }
    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "manager/change-password";
    }

    // Xử lý yêu cầu đổi mật khẩu
    @PostMapping("/change-password")
    public String processChangePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        // Lấy thông tin tài khoản quản lý đang đăng nhập
        String username = principal.getName();
        NhanVien manager = nhanVienRepository.findByTaiKhoan_TenDangNhap(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản quản lý."));

        TaiKhoan taiKhoan = manager.getTaiKhoan();

        // 1. Kiểm tra mật khẩu hiện tại có đúng không
        if (!passwordEncoder.matches(currentPassword, taiKhoan.getMatKhauHash())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không đúng!");
            return "redirect:/manager/change-password";
        }

        // 2. Kiểm tra mật khẩu mới có đủ dài không
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return "redirect:/manager/change-password";
        }

        // 3. Kiểm tra mật khẩu mới và mật khẩu xác nhận có khớp không
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới và mật khẩu xác nhận không khớp!");
            return "redirect:/manager/change-password";
        }

        // 4. Cập nhật mật khẩu mới
        taiKhoan.setMatKhauHash(passwordEncoder.encode(newPassword));
        taiKhoanRepository.save(taiKhoan);

        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        return "redirect:/manager/change-password";
    }
    @GetMapping("/settings")
    public String showSettingsPage() {
        return "manager/settings";
    }
}