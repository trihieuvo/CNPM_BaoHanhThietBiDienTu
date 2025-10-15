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
								HttpServletRequest request) {
		Optional<NhanVien> technicianOpt = nhanVienRepository.findByTaiKhoan_TenDangNhap(principal.getName());
		if (technicianOpt.isPresent()) {
			PageRequest pageable = PageRequest.of(page, size);
			Page<PhieuSuaChua> congViecPage;

			if (keyword == null || keyword.isEmpty()) {
				congViecPage = phieuSuaChuaRepository.findByKyThuatVien_MaNV(technicianOpt.get().getMaNV(), pageable);
			} else {
				congViecPage = phieuSuaChuaRepository.searchByKeywordForTechnician(technicianOpt.get().getMaNV(), keyword.toLowerCase(), pageable);
			}

			model.addAttribute("congViecPage", congViecPage);
			model.addAttribute("keyword", keyword);

			// Kiểm tra xem đây có phải là yêu cầu AJAX không
			String requestedWithHeader = request.getHeader("X-Requested-With");
			if ("XMLHttpRequest".equals(requestedWithHeader)) {
				return "technician/dashboard :: results-fragment"; // Nếu là AJAX, chỉ trả về fragment
			}
		}
		return "technician/dashboard"; // Nếu là tải trang bình thường, trả về toàn bộ trang
	}


	@GetMapping("/cong-viec/{id}")
	public String xemCongViec(@PathVariable("id") Integer id, Model model) {
		Optional<PhieuSuaChua> phieuSuaChuaOpt = phieuSuaChuaRepository.findById(id);
		if (phieuSuaChuaOpt.isPresent()) {
			PhieuSuaChua phieu = phieuSuaChuaOpt.get();

			// Lấy danh sách chi tiết linh kiện đã sử dụng
			List<ChiTietSuaChua> chiTietList = chiTietSuaChuaRepository.findByPhieuSuaChua_MaPhieu(id);

			// Lấy tất cả linh kiện để thêm mới
			List<LinhKien> allLinhKien = linhKienRepository.findAll();

			// KIỂM TRA PHIẾU ĐÃ HOÀN THÀNH CHƯA
			String trangThai = phieu.getTrangThai();
			List<String> trangThaiHoanThanh = Arrays.asList("Đã sửa xong", "Hoàn thành", "Đã trả khách");
			boolean isCompleted = trangThaiHoanThanh.contains(trangThai);

			model.addAttribute("phieu", phieu);
			model.addAttribute("chiTietList", chiTietList);
			model.addAttribute("allLinhKien", allLinhKien);
			model.addAttribute("isCompleted", isCompleted); // Gửi biến này sang view

			return "technician/cap-nhat-cong-viec";
		}
		return "redirect:/technician/dashboard";
	}

	@PostMapping("/cong-viec/delete/{id}")
	public String deletePhieuSuaChua(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes,
			Principal principal) {
		try {
			Optional<PhieuSuaChua> phieuOpt = phieuSuaChuaRepository.findById(id);
			Optional<NhanVien> technicianOpt = nhanVienRepository.findByTaiKhoan_TenDangNhap(principal.getName());

			if (phieuOpt.isPresent() && technicianOpt.isPresent()) {
				// Đảm bảo kỹ thuật viên chỉ có thể xóa phiếu của chính mình
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

			// Tính lại tổng chi phí
			phieu.tinhTongChiPhi();

			phieuSuaChuaRepository.save(phieu);
			redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");
		}
		return "redirect:/technician/cong-viec/" + maPhieu;
	}

	@PostMapping("/cong-viec/{maPhieu}/them-linh-kien")
	@Transactional
	@ResponseBody // Annotation quan trọng để trả về JSON
	public ResponseEntity<Map<String, Object>> themLinhKien(@PathVariable("maPhieu") Integer maPhieu,
			@RequestParam("maLinhKien") Integer maLinhKien, @RequestParam("soLuong") Integer soLuong) {
		Map<String, Object> response = new HashMap<>();
		try {
			Optional<PhieuSuaChua> phieuOpt = phieuSuaChuaRepository.findById(maPhieu);
			Optional<LinhKien> linhKienOpt = linhKienRepository.findById(maLinhKien);

			if (phieuOpt.isPresent() && linhKienOpt.isPresent()) {
				PhieuSuaChua phieu = phieuOpt.get();
				LinhKien linhKien = linhKienOpt.get();

				if (linhKien.getSoLuongTon() < soLuong) {
					response.put("status", "error");
					response.put("message", "Không đủ linh kiện trong kho! Tồn kho: " + linhKien.getSoLuongTon());
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}

				ChiTietSuaChua chiTiet = new ChiTietSuaChua();
				chiTiet.setPhieuSuaChua(phieu);
				chiTiet.setLinhKien(linhKien);
				chiTiet.setSoLuong(soLuong);
				chiTiet.setDonGia(linhKien.getDonGia());
				chiTiet.calculateThanhTien(); // Tính thành tiền

				// Lưu chi tiết trước để lấy ID
				ChiTietSuaChua savedChiTiet = chiTietSuaChuaRepository.save(chiTiet);

				// Cập nhật tồn kho
				linhKien.setSoLuongTon(linhKien.getSoLuongTon() - soLuong);
				linhKienRepository.save(linhKien);

				// Tính lại tổng chi phí và lưu phiếu
				phieu.tinhTongChiPhi();
				phieuSuaChuaRepository.save(phieu);

				// Chuẩn bị dữ liệu trả về
				response.put("status", "success");
				response.put("message", "Đã thêm linh kiện thành công!");
				response.put("chiTiet",
						Map.of("maChiTiet", savedChiTiet.getMaChiTiet(), "tenLinhKien", linhKien.getTenLinhKien(),
								"soLuong", soLuong, "donGia", linhKien.getDonGia(), "thanhTien",
								savedChiTiet.getThanhTien()));
				response.put("tongChiPhi", phieu.getTongChiPhi());

				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		} catch (Exception e) {
			response.put("status", "error");
			response.put("message", "Lỗi khi thêm linh kiện: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("status", "error");
		response.put("message", "Không tìm thấy phiếu hoặc linh kiện.");
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}

	// Xóa linh kiện khỏi phiếu sửa chữa - FIXED PROPERLY
	@PostMapping("/cong-viec/{maPhieu}/xoa-linh-kien/{maChiTiet}")
	@Transactional
	public String xoaLinhKien(@PathVariable("maPhieu") Integer maPhieu, @PathVariable("maChiTiet") Integer maChiTiet,
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

					redirectAttributes.addFlashAttribute("successMessage", "Đã xóa linh kiện thành công!");
				} else {
					redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chi tiết cần xóa!");
				}
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phiếu sửa chữa!");
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa linh kiện: " + e.getMessage());
			e.printStackTrace();
		}

		return "redirect:/technician/cong-viec/" + maPhieu;
	}
}