package com.baohanh.trungtambaohanh.service;

import com.baohanh.trungtambaohanh.entity.ChiTietSuaChua;
import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle; // Import PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List; // Import List
import java.util.Locale;

@Service
public class InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);
    // Bỏ biến instance font và document
    // private PDType0Font font;
    // private PDDocument document;

    public ByteArrayInputStream createInvoicePdf(PhieuSuaChua phieu) throws IOException {
        PDDocument document = new PDDocument(); // Tạo document cục bộ
        PDType0Font font = null;               // Khai báo font cục bộ
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PDPageContentStream contentStream = null;
        PDPage currentPage = null;

        try {
            // --- Load Font NGAY TẠI ĐÂY ---
            InputStream fontStream = null;
            try {
                // Sử dụng ClassPathResource để lấy InputStream từ classpath
                fontStream = new ClassPathResource("fonts/Roboto-Regular.ttf").getInputStream();
                // Load font với document hiện tại và nhúng (true)
                font = PDType0Font.load(document, fontStream, true);
                logger.debug("Font Roboto-Regular loaded successfully for document."); // Thêm log
            } catch (IOException e) {
                logger.error("Không thể load font tiếng Việt 'Roboto-Regular.ttf'.", e);
                // Ném lại lỗi hoặc xử lý phù hợp, ví dụ sử dụng font mặc định (nếu có)
                throw new IOException("Lỗi font: Không tìm thấy hoặc không load được font 'Roboto-Regular.ttf'.", e);
            } finally {
                // Đảm bảo InputStream được đóng
                if (fontStream != null) {
                    try {
                        fontStream.close();
                    } catch (IOException e) {
                        logger.warn("Không thể đóng font stream.", e);
                    }
                }
            }

            // Kiểm tra font đã load chưa
            if (font == null) {
                throw new IOException("Font is null after attempting to load.");
            }

            // --- Phần code vẽ PDF giữ nguyên, sử dụng biến 'font' cục bộ ---
            currentPage = new PDPage(PDRectangle.A4);
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            contentStream.setFont(font, 12); // Đặt font đã load

            float margin = 50;
            float yStart = currentPage.getMediaBox().getHeight() - margin;
            float yPosition = yStart;
            float leading = 1.5f * 12;

            // Định dạng tiền tệ và ngày giờ
            Locale localeVN = new Locale("vi", "VN");
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // Header
            contentStream.beginText();
            contentStream.setFont(font, 18);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("HÓA ĐƠN SỬA CHỮA");
            contentStream.endText();
            yPosition -= leading * 2;

            // Thông tin phiếu và khách hàng (truyền font vào)
            contentStream.setFont(font, 12);
            yPosition = drawCustomerInfo(contentStream, yPosition, margin, leading, phieu, dateFormatter, font);

            // Bảng Chi Tiết Sửa Chữa
            float tableTop = yPosition;
            float estimatedTableHeight = calculateTableHeight(phieu) + 20f; // + khoảng trắng

            if (tableTop - estimatedTableHeight < margin) {
                contentStream.close();
                currentPage = addNewPage(document); // Truyền document vào
                contentStream = new PDPageContentStream(document, currentPage);
                contentStream.setFont(font, 12);
                yPosition = currentPage.getMediaBox().getHeight() - margin;
                tableTop = yPosition;
            }

            // Vẽ bảng (font đã được truyền)
            drawTable(contentStream, font, tableTop, margin, phieu, currencyFormatter);
            yPosition = tableTop - calculateTableHeight(phieu) - leading; // Di chuyển yPosition xuống dưới bảng

            // Tổng cộng
            if (yPosition - leading * 2 < margin) {
                contentStream.close();
                currentPage = addNewPage(document); // Truyền document vào
                contentStream = new PDPageContentStream(document, currentPage);
                contentStream.setFont(font, 12);
                yPosition = currentPage.getMediaBox().getHeight() - margin;
            }

            contentStream.setFont(font, 14);
            contentStream.beginText();
            String totalText = "Tổng cộng: " + currencyFormatter.format(phieu.getTongChiPhi() != null ? phieu.getTongChiPhi() : 0);
            float textWidth = font.getStringWidth(totalText) / 1000 * 14;
            float totalX = currentPage.getMediaBox().getWidth() - margin - textWidth;
            contentStream.newLineAtOffset(totalX, yPosition);
            contentStream.showText(totalText);
            contentStream.endText();
            yPosition -= leading * 2;

            // Footer
            if (yPosition - leading < margin) {
                contentStream.close();
                currentPage = addNewPage(document); // Truyền document vào
                contentStream = new PDPageContentStream(document, currentPage);
                contentStream.setFont(font, 10);
                yPosition = margin + leading; // Đặt footer ở gần cuối trang
            }

            contentStream.setFont(font, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, margin); // Cách lề dưới margin
            contentStream.showText("Cảm ơn quý khách đã sử dụng dịch vụ!");
            contentStream.endText();

            // Đóng stream cuối cùng (kiểm tra null trước khi đóng)
            if (contentStream != null) {
                try {
                    contentStream.close();
                } catch (IOException e) {
                    logger.warn("Lỗi khi đóng contentStream cuối cùng.", e);
                }
            }


            document.save(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            logger.error("Lỗi nghiêm trọng khi tạo PDF hóa đơn cho phiếu #{}: {}", phieu.getMaPhieu(), e.getMessage(), e);
            // Đóng document nếu có lỗi xảy ra trước khi save
            if (document != null) {
                try { document.close(); } catch (IOException closeEx) { logger.error("Không thể đóng PDDocument sau lỗi.", closeEx); }
            }
            throw new IOException("Lỗi khi tạo file PDF hóa đơn: " + e.getMessage(), e);
        } finally {
            // Đảm bảo document luôn được đóng
            if (document != null) { // Chỉ cần kiểm tra xem document có null không
                try {
                    document.close(); // Gọi close trực tiếp
                } catch (IOException e) {
                    logger.error("Không thể đóng PDDocument trong finally.", e);
                }
            }
            // Đóng ByteArrayOutputStream
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("Không thể đóng ByteArrayOutputStream.", e);
                }
            }
        }
         // Dòng này thực ra sẽ không bao giờ đạt tới nếu code chạy đúng hoặc throw exception
         // return null; // Hoặc ném một ngoại lệ khác nếu logic cần
    }

     // --- Hàm thêm trang mới (cần nhận document) ---
     private PDPage addNewPage(PDDocument doc) {
         PDPage newPage = new PDPage(PDRectangle.A4);
         doc.addPage(newPage);
         logger.debug("Đã thêm trang mới vào PDF.");
         return newPage;
     }

     // --- Hàm vẽ thông tin khách hàng (cần nhận font) ---
     private float drawCustomerInfo(PDPageContentStream contentStream, float yPosition, float margin, float leading, PhieuSuaChua phieu, DateTimeFormatter dateFormatter, PDType0Font font) throws IOException {
         contentStream.setFont(font, 12); // Đảm bảo đặt font đúng

         contentStream.beginText();
         contentStream.newLineAtOffset(margin, yPosition);
         contentStream.showText("Mã phiếu: #" + phieu.getMaPhieu());
         contentStream.newLineAtOffset(300, 0); // Di chuyển sang phải
         contentStream.showText("Ngày xuất: " + OffsetDateTime.now().format(dateFormatter));
         contentStream.endText();
         yPosition -= leading;

         contentStream.beginText();
         contentStream.newLineAtOffset(margin, yPosition);
         contentStream.showText("Khách hàng: " + (phieu.getKhachHang() != null ? phieu.getKhachHang().getHoTen() : "N/A"));
         contentStream.newLineAtOffset(300, 0); // Di chuyển sang phải
         contentStream.showText("Điện thoại: " + (phieu.getKhachHang() != null ? phieu.getKhachHang().getSoDienThoai() : "N/A"));
         contentStream.endText();
         yPosition -= leading;

         contentStream.beginText();
         contentStream.newLineAtOffset(margin, yPosition);
         contentStream.showText("Địa chỉ: " + (phieu.getKhachHang() != null && phieu.getKhachHang().getDiaChi() != null ? phieu.getKhachHang().getDiaChi() : "N/A"));
         contentStream.endText();
         yPosition -= leading;

         contentStream.beginText();
         contentStream.newLineAtOffset(margin, yPosition);
         contentStream.showText("Thiết bị: " + (phieu.getThietBi() != null ? (phieu.getThietBi().getHangSanXuat()+" "+phieu.getThietBi().getModel()) : "N/A"));
         contentStream.newLineAtOffset(300, 0); // Di chuyển sang phải
         contentStream.showText("Serial: " + (phieu.getThietBi() != null && phieu.getThietBi().getSoSerial() != null ? phieu.getThietBi().getSoSerial() : "N/A"));
         contentStream.endText();
         yPosition -= leading * 2; // Thêm khoảng cách trước bảng
         return yPosition;
     }

     // --- Hàm tính chiều cao ước lượng của bảng ---
     private float calculateTableHeight(PhieuSuaChua phieu) {
         List<ChiTietSuaChua> chiTietList = phieu.getChiTietSuaChuaList();
         if (chiTietList == null) {
             chiTietList = List.of(); // Coi như list rỗng
         }
         final float rowHeight = 20f;
         return (chiTietList.size() + 1) * rowHeight; // +1 cho header
     }

    // --- Hàm vẽ bảng (Helper method - Giữ nguyên logic vẽ, đã nhận font) ---
    private void drawTable(PDPageContentStream contentStream, PDType0Font font, float y, float margin, PhieuSuaChua phieu, NumberFormat currencyFormatter) throws IOException {
        List<ChiTietSuaChua> chiTietList = phieu.getChiTietSuaChuaList();
        if (chiTietList == null) {
            chiTietList = List.of(); // Khởi tạo nếu null
            logger.warn("Danh sách chi tiết sửa chữa cho phiếu #{} là null.", phieu.getMaPhieu());
        }

        final int rows = chiTietList.size() + 1;
        final int cols = 4;
        final float rowHeight = 20f;
        final float tableWidth = 500f; // Điều chỉnh chiều rộng bảng nếu cần
        // Điều chỉnh chiều rộng cột cho phù hợp
        final float[] colWidths = {200f, 80f, 110f, 110f};
         if (colWidths.length != cols) {
             throw new IllegalArgumentException("Số lượng chiều rộng cột không khớp số cột");
         }
         float sumWidth = 0;
         for(float w : colWidths) sumWidth += w;
         // Kiểm tra tổng chiều rộng cột có khớp chiều rộng bảng không
         if(Math.abs(sumWidth - tableWidth) > 0.1) {
            logger.warn("Tổng chiều rộng các cột ({}) không bằng chiều rộng bảng ({})", sumWidth, tableWidth);
            // Có thể điều chỉnh lại tableWidth = sumWidth; nếu muốn bảng tự co dãn theo cột
         }

        final float cellMargin = 5f;

        // Vẽ đường kẻ ngang
        float nextY = y;
        for (int i = 0; i <= rows; i++) {
            contentStream.moveTo(margin, nextY);
            contentStream.lineTo(margin + tableWidth, nextY);
            contentStream.stroke();
            nextY -= rowHeight;
        }

        // Vẽ đường kẻ dọc
        float nextX = margin;
        for (int i = 0; i <= cols; i++) {
            contentStream.moveTo(nextX, y);
            contentStream.lineTo(nextX, y - rows * rowHeight);
            contentStream.stroke();
            if (i < cols) {
                 nextX += colWidths[i];
            }
        }

        // Header của bảng
        float textX = margin + cellMargin;
        float textY = y - 15; // Điều chỉnh vị trí Y cho text trong header
        String[] headers = {"Tên mục", "Số lượng", "Đơn giá", "Thành tiền"};
        contentStream.setFont(font, 10); // Đặt font cho header

        // Vẽ text header với căn chỉnh
         for (int i = 0; i < headers.length; i++) {
             contentStream.beginText();
             float currentX = textX;
             float headerWidth = font.getStringWidth(headers[i]) / 1000 * 10; // Cỡ chữ 10
             // Căn chỉnh text header
             if (i == 1) { // Cột Số lượng (căn giữa)
                 currentX = textX + (colWidths[i] / 2) - (headerWidth / 2);
             } else if (i > 1) { // Cột Đơn giá, Thành tiền (căn phải)
                  currentX = textX + colWidths[i] - headerWidth - cellMargin * 2; // Thêm *2 để cách lề phải
             }
             contentStream.newLineAtOffset(currentX, textY);
             contentStream.showText(headers[i]);
             contentStream.endText();
             textX += colWidths[i]; // Di chuyển X sang cột tiếp theo
         }
        textY -= rowHeight; // Di chuyển Y xuống dòng dữ liệu đầu tiên

        // Dữ liệu chi tiết
        for (ChiTietSuaChua ct : chiTietList) {
             textX = margin + cellMargin; // Reset X về đầu dòng

             // Cột Tên mục (căn trái)
             contentStream.beginText();
             contentStream.newLineAtOffset(textX, textY);
             String tenLinhKien = ct.getLinhKien() != null ? ct.getLinhKien().getTenLinhKien() : "N/A";
             // Rút gọn tên linh kiện nếu quá dài
             float maxWidthName = colWidths[0] - 2 * cellMargin;
             float nameWidth = font.getStringWidth(tenLinhKien) / 1000 * 10;
             if (nameWidth > maxWidthName) {
                 // Tính toán số ký tự giữ lại dựa trên tỷ lệ chiều rộng
                 int charsToKeep = (int) (tenLinhKien.length() * (maxWidthName / nameWidth)) - 3; // Trừ 3 cho "..."
                 charsToKeep = Math.max(0, charsToKeep); // Đảm bảo không âm
                 tenLinhKien = tenLinhKien.substring(0, charsToKeep) + "...";
             }
             contentStream.showText(tenLinhKien);
             contentStream.endText();
             textX += colWidths[0];

             // Cột Số lượng (căn giữa)
             String soLuongStr = String.valueOf(ct.getSoLuong() != null ? ct.getSoLuong() : 0);
             float slWidth = font.getStringWidth(soLuongStr) / 1000 * 10;
             contentStream.beginText();
             // Tính toán vị trí X để căn giữa
             contentStream.newLineAtOffset(textX + (colWidths[1] / 2) - (slWidth / 2), textY);
             contentStream.showText(soLuongStr);
             contentStream.endText();
             textX += colWidths[1];

             // Cột Đơn giá (căn phải)
             String donGiaStr = currencyFormatter.format(ct.getDonGia() != null ? ct.getDonGia() : java.math.BigDecimal.ZERO);
             float dgWidth = font.getStringWidth(donGiaStr) / 1000 * 10;
             contentStream.beginText();
             // Tính toán vị trí X để căn phải
             contentStream.newLineAtOffset(textX + colWidths[2] - dgWidth - cellMargin * 2, textY); // Thêm *2
             contentStream.showText(donGiaStr);
             contentStream.endText();
             textX += colWidths[2];

             // Cột Thành tiền (căn phải)
             String thanhTienStr = currencyFormatter.format(ct.getThanhTien() != null ? ct.getThanhTien() : java.math.BigDecimal.ZERO);
             float ttWidth = font.getStringWidth(thanhTienStr) / 1000 * 10;
             contentStream.beginText();
             // Tính toán vị trí X để căn phải
             contentStream.newLineAtOffset(textX + colWidths[3] - ttWidth - cellMargin * 2, textY); // Thêm *2
             contentStream.showText(thanhTienStr);
             contentStream.endText();

             textY -= rowHeight; // Di chuyển Y xuống dòng tiếp theo
         }
    }
}