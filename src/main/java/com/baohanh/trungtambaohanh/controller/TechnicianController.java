package com.baohanh.trungtambaohanh.controller;

import com.baohanh.trungtambaohanh.entity.ChiTietSuaChua;
import com.baohanh.trungtambaohanh.entity.LinhKien;
import com.baohanh.trungtambaohanh.entity.NhanVien;
import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import com.baohanh.trungtambaohanh.repository.ChiTietSuaChuaRepository;
import com.baohanh.trungtambaohanh.repository.LinhKienRepository;
import com.baohanh.trungtambaohanh.repository.NhanVienRepository;
import com.baohanh.trungtambaohanh.repository.PhieuSuaChuaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/technician")
public class TechnicianController {

    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final NhanVienRepository nhanVienRepository;
    private final LinhKienRepository linhKienRepository;
    private final ChiTietSuaChuaRepository chiTietSuaChuaRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    public TechnicianController(PhieuSuaChuaRepository phieuSuaChuaRepository, 
                               NhanVienRepository nhanVienRepository, 
                               LinhKienRepository linhKienRepository,
                               ChiTietSuaChuaRepository chiTietSuaChuaRepository) {
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.linhKienRepository = linhKienRepository;
        this.chiTietSuaChuaRepository = chiTietSuaChuaRepository;
    }

    // Danh sách công việc
    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal) {
        Optional<NhanVien> technicianOpt = nhanVienRepository.findByTaiKhoan_TenDangNhap(principal.getName());
        if (technicianOpt.isPresent()) {
            List<PhieuSuaChua> congViecList = phieuSuaChuaRepository.findByKyThuatVien_MaNV(technicianOpt.get().getMaNV());
            model.addAttribute("congViecList", congViecList);
            return "technician/dashboard";
        }
        return "redirect:/logout";
    }
    
    // Xem chi tiết và cập nhật tiến độ
    @GetMapping("/cong-viec/{id}")
    public String xemCongViec(@PathVariable("id") Integer id, Model model) {
        Optional<PhieuSuaChua> phieuSuaChuaOpt = phieuSuaChuaRepository.findById(id);
        if (phieuSuaChuaOpt.isPresent()) {
            PhieuSuaChua phieu = phieuSuaChuaOpt.get();
            
            // Lấy danh sách chi tiết linh kiện đã sử dụng
            List<ChiTietSuaChua> chiTietList = chiTietSuaChuaRepository.findByPhieuSuaChua_MaPhieu(id);
            
            // Lấy tất cả linh kiện để thêm mới
            List<LinhKien> allLinhKien = linhKienRepository.findAll();
            
            model.addAttribute("phieu", phieu);
            model.addAttribute("chiTietList", chiTietList);
            model.addAttribute("allLinhKien", allLinhKien);
            
            return "technician/cap-nhat-cong-viec";
        }
        return "redirect:/technician/dashboard";
    }

    @PostMapping("/cong-viec/update")
    @Transactional
    public String capNhatCongViec(@RequestParam("maPhieu") Integer maPhieu,
                                  @RequestParam("trangThai") String trangThai,
                                  @RequestParam("ghiChuKyThuat") String ghiChuKyThuat,
                                  RedirectAttributes redirectAttributes) {
        Optional<PhieuSuaChua> phieuSuaChuaOpt = phieuSuaChuaRepository.findById(maPhieu);
        if (phieuSuaChuaOpt.isPresent()) {
            PhieuSuaChua phieu = phieuSuaChuaOpt.get();
            phieu.setTrangThai(trangThai);
            phieu.setGhiChuKyThuat(ghiChuKyThuat);
            
            // Tính lại tổng chi phí
            phieu.tinhTongChiPhi();
            
            phieuSuaChuaRepository.save(phieu);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");
        }
        return "redirect:/technician/cong-viec/" + maPhieu;
    }

    // Thêm linh kiện vào phiếu sửa chữa
    @PostMapping("/cong-viec/{maPhieu}/them-linh-kien")
    @Transactional
    public String themLinhKien(@PathVariable("maPhieu") Integer maPhieu,
                              @RequestParam("maLinhKien") Integer maLinhKien,
                              @RequestParam("soLuong") Integer soLuong,
                              RedirectAttributes redirectAttributes) {
        try {
            Optional<PhieuSuaChua> phieuOpt = phieuSuaChuaRepository.findById(maPhieu);
            Optional<LinhKien> linhKienOpt = linhKienRepository.findById(maLinhKien);
            
            if (phieuOpt.isPresent() && linhKienOpt.isPresent()) {
                PhieuSuaChua phieu = phieuOpt.get();
                LinhKien linhKien = linhKienOpt.get();
                
                // Kiểm tra tồn kho
                if (linhKien.getSoLuongTon() < soLuong) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Không đủ linh kiện trong kho! Tồn kho: " + linhKien.getSoLuongTon());
                    return "redirect:/technician/cong-viec/" + maPhieu;
                }
                
                // Tạo chi tiết sửa chữa
                ChiTietSuaChua chiTiet = new ChiTietSuaChua();
                chiTiet.setPhieuSuaChua(phieu);
                chiTiet.setLinhKien(linhKien);
                chiTiet.setSoLuong(soLuong);
                chiTiet.setDonGia(linhKien.getDonGia());
                
                // Thêm vào collection của phiếu
                phieu.getChiTietSuaChuaList().add(chiTiet);
                
                // Cập nhật tồn kho
                linhKien.setSoLuongTon(linhKien.getSoLuongTon() - soLuong);
                linhKienRepository.save(linhKien);
                
                // Tính lại tổng chi phí
                phieu.tinhTongChiPhi();
                phieuSuaChuaRepository.save(phieu);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã thêm linh kiện thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi khi thêm linh kiện: " + e.getMessage());
        }
        
        return "redirect:/technician/cong-viec/" + maPhieu;
    }
    
    // Xóa linh kiện khỏi phiếu sửa chữa - FIXED PROPERLY
    @PostMapping("/cong-viec/{maPhieu}/xoa-linh-kien/{maChiTiet}")
    @Transactional
    public String xoaLinhKien(@PathVariable("maPhieu") Integer maPhieu,
                             @PathVariable("maChiTiet") Integer maChiTiet,
                             RedirectAttributes redirectAttributes) {
        try {
            // Load phiếu sửa chữa
            Optional<PhieuSuaChua> phieuOpt = phieuSuaChuaRepository.findById(maPhieu);
            
            if (phieuOpt.isPresent()) {
                PhieuSuaChua phieu = phieuOpt.get();
                
                // Tìm chi tiết cần xóa trong collection
                ChiTietSuaChua chiTietCanXoa = null;
                for (ChiTietSuaChua ct : phieu.getChiTietSuaChuaList()) {
                    if (ct.getMaChiTiet().equals(maChiTiet)) {
                        chiTietCanXoa = ct;
                        break;
                    }
                }
                
                if (chiTietCanXoa != null) {
                    // Lưu thông tin để hoàn trả kho
                    Integer soLuongHoanTra = chiTietCanXoa.getSoLuong();
                    Integer maLinhKien = chiTietCanXoa.getLinhKien().getMaLinhKien();
                    
                    // Xóa từ collection - orphanRemoval sẽ tự động xóa khỏi DB
                    phieu.getChiTietSuaChuaList().remove(chiTietCanXoa);
                    chiTietCanXoa.setPhieuSuaChua(null);
                    
                    // Hoàn trả số lượng vào kho
                    Optional<LinhKien> linhKienOpt = linhKienRepository.findById(maLinhKien);
                    if (linhKienOpt.isPresent()) {
                        LinhKien linhKien = linhKienOpt.get();
                        linhKien.setSoLuongTon(linhKien.getSoLuongTon() + soLuongHoanTra);
                        linhKienRepository.save(linhKien);
                    }
                    
                    // Tính lại tổng chi phí
                    phieu.tinhTongChiPhi();
                    phieuSuaChuaRepository.save(phieu);
                    
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Đã xóa linh kiện thành công!");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Không tìm thấy chi tiết cần xóa!");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Không tìm thấy phiếu sửa chữa!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi khi xóa linh kiện: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/technician/cong-viec/" + maPhieu;
    }

    // Tra cứu linh kiện
    @GetMapping("/linh-kien")
    public String traCuuLinhKienPage(@RequestParam(value = "keyword", required = false) String keyword, 
                                     Model model) {
        if (keyword != null && !keyword.isEmpty()) {
            List<LinhKien> ketQua = linhKienRepository.findByTenLinhKienContainingIgnoreCase(keyword);
            model.addAttribute("linhKienList", ketQua);
        } else {
            model.addAttribute("linhKienList", linhKienRepository.findAll());
        }
        model.addAttribute("keyword", keyword);
        return "technician/tra-cuu-linh-kien";
    }
}