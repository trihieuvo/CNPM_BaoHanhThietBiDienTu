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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.HashMap;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Sort;
@Controller
@RequestMapping("/technician")
public class TechnicianController {

	private final PhieuSuaChuaRepository phieuSuaChuaRepository;
	private final NhanVienRepository nhanVienRepository;
	private final LinhKienRepository linhKienRepository;
	private final ChiTietSuaChuaRepository chiTietSuaChuaRepository;

	@PersistenceContext
	private EntityManager entityManager;

	public TechnicianController(PhieuSuaChuaRepository phieuSuaChuaRepository, NhanVienRepository nhanVienRepository,
			LinhKienRepository linhKienRepository, ChiTietSuaChuaRepository chiTietSuaChuaRepository) {
		this.phieuSuaChuaRepository = phieuSuaChuaRepository;
		this.nhanVienRepository = nhanVienRepository;
		this.linhKienRepository = linhKienRepository;
		this.chiTietSuaChuaRepository = chiTietSuaChuaRepository;
	}

	@GetMapping("/dashboard")
	public String showDashboard(Model model, Principal principal,
	                            @RequestParam(name = "page", defaultValue = "0") int page,
	                            @RequestParam(name = "size", defaultValue = "6") int size,
	                            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
	                            @RequestParam(name = "sortField", defaultValue = "ngayTiepNhan") String sortField,
	                            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir,
	                            HttpServletRequest request) {
	    Optional<NhanVien> technicianOpt = nhanVienRepository.findByTaiKhoan_TenDangNhap(principal.getName());
	    if (technicianOpt.isPresent()) {
	        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
	        PageRequest pageable = PageRequest.of(page, size, sort);
	        Page<PhieuSuaChua> congViecPage;

	        if (keyword == null || keyword.isEmpty()) {
	            congViecPage = phieuSuaChuaRepository.findByKyThuatVien_MaNV(technicianOpt.get().getMaNV(), pageable);
	        } else {
	            congViecPage = phieuSuaChuaRepository.searchByKeywordForTechnician(technicianOpt.get().getMaNV(), keyword.toLowerCase(), pageable);
	        }

	        model.addAttribute("congViecPage", congViecPage);
	        model.addAttribute("keyword", keyword);
	        
	        model.addAttribute("sortField", sortField);
	        model.addAttribute("sortDir", sortDir);
	        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

	        String requestedWithHeader = request.getHeader("X-Requested-With");
	        if ("XMLHttpRequest".equals(requestedWithHeader)) {
	            return "technician/dashboard :: results-fragment";
	        }
	    }
	    return "technician/dashboard";
	}


	@GetMapping("/cong-viec/{id}")
	public String xemCongViec(@PathVariable("id") Integer id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
	    Optional<PhieuSuaChua> phieuSuaChuaOpt = phieuSuaChuaRepository.findById(id);
	    Optional<NhanVien> technicianOpt = nhanVienRepository.findByTaiKhoan_TenDangNhap(principal.getName());

	    if (phieuSuaChuaOpt.isPresent() && technicianOpt.isPresent()) {
	        PhieuSuaChua phieu = phieuSuaChuaOpt.get();
	        NhanVien technician = technicianOpt.get();

	        if (phieu.getKyThuatVien() == null || !phieu.getKyThuatVien().getMaNV().equals(technician.getMaNV())) {
	            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập phiếu sửa chữa này.");
	            return "redirect:/technician/dashboard";
	        }

	        List<ChiTietSuaChua> chiTietList = chiTietSuaChuaRepository.findByPhieuSuaChua_MaPhieu(id);
	        List<LinhKien> allLinhKien = linhKienRepository.findAll();
	        String trangThai = phieu.getTrangThai();
            
            // ====[ BẮT ĐẦU THAY ĐỔI ]====
	        // Thêm "Trả về - không sửa được" vào danh sách các trạng thái cuối cùng
	        List<String> trangThaiHoanThanh = Arrays.asList("Đã sửa xong", "Hoàn thành", "Đã trả khách", "Trả về - không sửa được");
	        // ====[ KẾT THÚC THAY ĐỔI ]====

	        boolean isCompleted = trangThaiHoanThanh.contains(trangThai);

	        model.addAttribute("phieu", phieu);
	        model.addAttribute("chiTietList", chiTietList);
	        model.addAttribute("allLinhKien", allLinhKien);
	        model.addAttribute("isCompleted", isCompleted);

	        return "technician/cap-nhat-cong-viec";
	    }

	    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phiếu sửa chữa.");
	    return "redirect:/technician/dashboard";
	}

	@PostMapping("/cong-viec/delete/{id}")
	public String deletePhieuSuaChua(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes,
			Principal principal) {
		try {
			Optional<PhieuSuaChua> phieuOpt = phieuSuaChuaRepository.findById(id);
			Optional<NhanVien> technicianOpt = nhanVienRepository.findByTaiKhoan_TenDangNhap(principal.getName());

			if (phieuOpt.isPresent() && technicianOpt.isPresent()) {
				if (phieuOpt.get().getKyThuatVien().getMaNV().equals(technicianOpt.get().getMaNV())) {
					phieuSuaChuaRepository.deleteById(id);
					redirectAttributes.addFlashAttribute("successMessage",
							"Đã xóa phiếu sửa chữa #" + id + " thành công.");
				} else {
					redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa phiếu này.");
				}
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phiếu sửa chữa.");
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa phiếu: " + e.getMessage());
		}
		return "redirect:/technician/dashboard";
	}

	@PostMapping("/cong-viec/update")
	@Transactional
	public String capNhatCongViec(@RequestParam("maPhieu") Integer maPhieu, @RequestParam("trangThai") String trangThai,
			@RequestParam("ghiChuKyThuat") String ghiChuKyThuat, RedirectAttributes redirectAttributes) {
		Optional<PhieuSuaChua> phieuSuaChuaOpt = phieuSuaChuaRepository.findById(maPhieu);
		if (phieuSuaChuaOpt.isPresent()) {
			PhieuSuaChua phieu = phieuSuaChuaOpt.get();
			phieu.setTrangThai(trangThai);
			phieu.setGhiChuKyThuat(ghiChuKyThuat);
			phieu.tinhTongChiPhi();
			phieuSuaChuaRepository.save(phieu);
			redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");
		}
		return "redirect:/technician/cong-viec/" + maPhieu;
	}

	@PostMapping("/cong-viec/{maPhieu}/them-linh-kien")
	@Transactional
	@ResponseBody
	public ResponseEntity<Map<String, Object>> themLinhKien(@PathVariable("maPhieu") Integer maPhieu,
			@RequestParam("maLinhKien") Integer maLinhKien, @RequestParam("soLuong") Integer soLuong) {
		Map<String, Object> response = new HashMap<>();
		try {
			PhieuSuaChua phieu = phieuSuaChuaRepository.findById(maPhieu)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu sửa chữa."));
			LinhKien linhKien = linhKienRepository.findById(maLinhKien)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy linh kiện."));

			if (linhKien.getSoLuongTon() < soLuong) {
				response.put("status", "error");
				response.put("message", "Không đủ linh kiện trong kho! Tồn kho: " + linhKien.getSoLuongTon());
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			Optional<ChiTietSuaChua> existingChiTietOpt = chiTietSuaChuaRepository
					.findByPhieuSuaChua_MaPhieuAndLinhKien_MaLinhKien(maPhieu, maLinhKien);

			ChiTietSuaChua chiTiet;
			if (existingChiTietOpt.isPresent()) {
				chiTiet = existingChiTietOpt.get();
				chiTiet.setSoLuong(chiTiet.getSoLuong() + soLuong);
			} else {
				chiTiet = new ChiTietSuaChua();
				chiTiet.setPhieuSuaChua(phieu);
				chiTiet.setLinhKien(linhKien);
				chiTiet.setSoLuong(soLuong);
				chiTiet.setDonGia(linhKien.getDonGia());
			}
			
			chiTiet.calculateThanhTien();
			chiTietSuaChuaRepository.save(chiTiet);

			linhKien.setSoLuongTon(linhKien.getSoLuongTon() - soLuong);
			linhKienRepository.save(linhKien);

			entityManager.flush();
			entityManager.refresh(phieu);

			phieu.tinhTongChiPhi();
			phieuSuaChuaRepository.save(phieu);

			response.put("status", "success");
			response.put("message", "Đã cập nhật chi phí thành công!");
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			response.put("status", "error");
			response.put("message", "Lỗi khi cập nhật chi phí: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/cong-viec/{maPhieu}/xoa-linh-kien/{maChiTiet}")
	@Transactional
	public String xoaLinhKien(@PathVariable("maPhieu") Integer maPhieu,
							  @PathVariable("maChiTiet") Integer maChiTiet,
							  RedirectAttributes redirectAttributes) {
		try {
			ChiTietSuaChua chiTiet = chiTietSuaChuaRepository.findById(maChiTiet)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết cần xóa!"));
			
			LinhKien linhKien = chiTiet.getLinhKien();
			linhKien.setSoLuongTon(linhKien.getSoLuongTon() + 1);
			linhKienRepository.save(linhKien);

			if (chiTiet.getSoLuong() > 1) {
				chiTiet.setSoLuong(chiTiet.getSoLuong() - 1);
				chiTietSuaChuaRepository.save(chiTiet);
			} else {
				chiTietSuaChuaRepository.delete(chiTiet);
			}

			PhieuSuaChua phieu = phieuSuaChuaRepository.findById(maPhieu)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu sửa chữa!"));
			
			entityManager.flush();
			entityManager.refresh(phieu);
			
			phieu.tinhTongChiPhi();
			phieuSuaChuaRepository.save(phieu);

			redirectAttributes.addFlashAttribute("successMessage", "Đã xóa 1 đơn vị thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa linh kiện: " + e.getMessage());
			e.printStackTrace();
		}
		return "redirect:/technician/cong-viec/" + maPhieu;
	}

    @PostMapping("/cong-viec/mark-unrepairable")
    @Transactional
    public String markAsUnrepairable(@RequestParam("maPhieu") Integer maPhieu,
                                     @RequestParam(value = "ghiChuKyThuat", required = false) String ghiChuKyThuat,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {

        Optional<PhieuSuaChua> phieuSuaChuaOpt = phieuSuaChuaRepository.findById(maPhieu);
        Optional<NhanVien> technicianOpt = nhanVienRepository.findByTaiKhoan_TenDangNhap(principal.getName());

        if (phieuSuaChuaOpt.isPresent() && technicianOpt.isPresent()) {
            PhieuSuaChua phieu = phieuSuaChuaOpt.get();
            NhanVien technician = technicianOpt.get();

            if (phieu.getKyThuatVien() == null || !phieu.getKyThuatVien().getMaNV().equals(technician.getMaNV())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện thao tác này.");
                return "redirect:/technician/dashboard";
            }

            try {
                phieu.setTrangThai("Trả về - không sửa được");
                phieu.setGhiChuKyThuat(ghiChuKyThuat);
                phieuSuaChuaRepository.save(phieu);

                redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật phiếu #" + maPhieu + " thành 'Trả về - không sửa được'.");
                return "redirect:/technician/dashboard";

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật phiếu: " + e.getMessage());
                return "redirect:/technician/cong-viec/" + maPhieu;
            }
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phiếu sửa chữa.");
        return "redirect:/technician/dashboard";
    }
}