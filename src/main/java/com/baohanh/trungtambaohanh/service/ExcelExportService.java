package com.baohanh.trungtambaohanh.service;

import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    public ByteArrayInputStream createRevenueReportExcel(List<PhieuSuaChua> tickets) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            XSSFSheet sheet = workbook.createSheet("BaoCaoDoanhThu");

            // --- Tạo Header ---
            String[] headers = {"Mã Phiếu", "Khách Hàng", "Thiết Bị", "KTV Phụ Trách", "Ngày Hoàn Thành", "Tổng Chi Phí (VNĐ)"};
            Row headerRow = sheet.createRow(0);

            // Style cho header
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // --- Đổ dữ liệu ---
            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (PhieuSuaChua ticket : tickets) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(ticket.getMaPhieu());
                row.createCell(1).setCellValue(ticket.getKhachHang() != null ? ticket.getKhachHang().getHoTen() : "N/A");
                row.createCell(2).setCellValue(ticket.getThietBi() != null ? ticket.getThietBi().getModel() : "N/A");
                row.createCell(3).setCellValue(ticket.getKyThuatVien() != null ? ticket.getKyThuatVien().getHoTen() : "Chưa gán");
                row.createCell(4).setCellValue(ticket.getNgayHoanThanh() != null ? ticket.getNgayHoanThanh().format(formatter) : "N/A");
                row.createCell(5).setCellValue(ticket.getTongChiPhi() != null ? ticket.getTongChiPhi().doubleValue() : 0.0);
            }
            
            // Tự động điều chỉnh kích thước cột
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tạo file Excel: " + e.getMessage());
        }
    }
}