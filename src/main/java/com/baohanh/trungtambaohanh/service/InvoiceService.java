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
    private PDType0Font font; // Đưa font ra ngoài để load 1 lần
    private PDDocument document; // Đưa document ra ngoài để quản lý trang

    public ByteArrayInputStream createInvoicePdf(PhieuSuaChua phieu) throws IOException {
        document = new PDDocument(); // Khởi tạo document ở đây
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PDPageContentStream contentStream = null; // Khai báo ở ngoài
        PDPage currentPage = null; // Trang hiện tại

        try {
            // --- Load Font (Chỉ load 1 lần) ---
            loadFont(); // Gọi hàm load font

            currentPage = new PDPage(PDRectangle.A4); // Tạo trang đầu tiên A4
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage); // Mở stream cho trang đầu
            contentStream.setFont(font, 12); // Đặt font

            float margin = 50;
            float yStart = currentPage.getMediaBox().getHeight() - margin;
            float yPosition = yStart;
            float leading = 1.5f * 12;

            // --- Định dạng tiền tệ và ngày giờ ---
            Locale localeVN = new Locale("vi", "VN");
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // --- Header ---
            contentStream.beginText();
            contentStream.setFont(font, 18);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("HÓA ĐƠN SỬA CHỮA");
            contentStream.endText();
            yPosition -= leading * 2;

            // --- Thông tin phiếu và khách hàng ---
            contentStream.setFont(font, 12);
            yPosition = drawCustomerInfo(contentStream, yPosition, margin, leading, phieu, dateFormatter);


            // --- Bảng Chi Tiết Sửa Chữa ---
            float tableTop = yPosition;
            float estimatedTableHeight = calculateTableHeight(phieu) + 20f; // + khoảng trắng

            // Kiểm tra nếu bảng không vừa trang hiện tại
            if (tableTop - estimatedTableHeight < margin) {
                contentStream.close(); // Đóng stream trang cũ
                currentPage = addNewPage(); // Tạo trang mới
                contentStream = new PDPageContentStream(document, currentPage); // Mở stream mới
                contentStream.setFont(font, 12); // Đặt lại font
                yPosition = currentPage.getMediaBox().getHeight() - margin; // Reset yPosition
                tableTop = yPosition;
            }

            // Vẽ bảng
            drawTable(contentStream, font, tableTop, margin, phieu, currencyFormatter);
            // === SỬA LỖI: Trừ thêm khoảng cách (leading) để Tổng cộng không bị đè ===
            yPosition = tableTop - calculateTableHeight(phieu) - leading; // Di chuyển yPosition xuống dưới bảng
            // ===================================================================

            // --- Tổng cộng ---
            // Kiểm tra nếu Tổng cộng không vừa trang hiện tại (kiểm tra sau khi đã trừ yPosition)
            if (yPosition - leading * 2 < margin) {
                contentStream.close();
                currentPage = addNewPage();
                contentStream = new PDPageContentStream(document, currentPage);
                contentStream.setFont(font, 12);
                yPosition = currentPage.getMediaBox().getHeight() - margin;
            }

            contentStream.setFont(font, 14);
            contentStream.beginText();
            String totalText = "Tổng cộng: " + currencyFormatter.format(phieu.getTongChiPhi() != null ? phieu.getTongChiPhi() : 0);
            float textWidth = font.getStringWidth(totalText) / 1000 * 14;
            float totalX = currentPage.getMediaBox().getWidth() - margin - textWidth;
            contentStream.newLineAtOffset(totalX, yPosition); // yPosition bây giờ đã ở dưới bảng
            contentStream.showText(totalText);
            contentStream.endText();
            yPosition -= leading * 2; // Di chuyển xuống cho nội dung tiếp theo (Footer)

            // --- Footer ---
            // Kiểm tra nếu Footer không vừa trang hiện tại
             if (yPosition - leading < margin) {
                 contentStream.close();
                 currentPage = addNewPage();
                 contentStream = new PDPageContentStream(document, currentPage);
                 contentStream.setFont(font, 10);
                 // yPosition = currentPage.getMediaBox().getHeight() - margin; // Reset y cho trang mới, nếu muốn footer ở trên cùng trang mới
                 yPosition = margin + leading; // Đặt footer ở gần cuối trang hiện tại (hoặc trang mới nếu vừa tạo)
             }

            contentStream.setFont(font, 10);
            contentStream.beginText();
             // Đặt footer ở dưới cùng của trang hiện tại
             contentStream.newLineAtOffset(margin, margin); // Cách lề dưới margin
            contentStream.showText("Cảm ơn quý khách đã sử dụng dịch vụ!");
            contentStream.endText();

            // Đóng stream cuối cùng
            if (contentStream != null) {
                contentStream.close();
            }

            document.save(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            logger.error("Lỗi nghiêm trọng khi tạo PDF hóa đơn cho phiếu #{}: {}", phieu.getMaPhieu(), e.getMessage(), e);
            throw new IOException("Lỗi khi tạo file PDF hóa đơn: " + e.getMessage(), e);
        } finally {
            // Đảm bảo document luôn được đóng
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    logger.error("Không thể đóng PDDocument.", e);
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
    }

    // --- Hàm load font tách biệt ---
    private void loadFont() throws IOException {
        if (font == null) { // Chỉ load nếu chưa load
            InputStream fontStream = null;
            try {
                fontStream = new ClassPathResource("fonts/Roboto-Regular.ttf").getInputStream();
                // Sử dụng document đã khai báo ở ngoài
                font = PDType0Font.load(document, fontStream, false);
            } catch (IOException e) {
                logger.error("Không thể load font tiếng Việt 'Roboto-Regular.ttf'.", e);
                throw new IOException("Lỗi font: Không tìm thấy hoặc không load được font 'Roboto-Regular.ttf'.", e);
            } finally {
                if (fontStream != null) {
                    try {
                        fontStream.close();
                    } catch (IOException e) {
                        logger.warn("Không thể đóng font stream.", e);
                    }
                }
            }
        }
    }

     // --- Hàm thêm trang mới ---
     private PDPage addNewPage() {
         PDPage newPage = new PDPage(PDRectangle.A4);
         document.addPage(newPage);
         logger.debug("Đã thêm trang mới vào PDF."); // Thêm log để debug
         return newPage;
     }

     // --- Hàm vẽ thông tin khách hàng ---
     private float drawCustomerInfo(PDPageContentStream contentStream, float yPosition, float margin, float leading, PhieuSuaChua phieu, DateTimeFormatter dateFormatter) throws IOException {
         contentStream.beginText();
         contentStream.newLineAtOffset(margin, yPosition);
         contentStream.showText("Mã phiếu: #" + phieu.getMaPhieu());
         contentStream.newLineAtOffset(300, 0);
         contentStream.showText("Ngày xuất: " + OffsetDateTime.now().format(dateFormatter));
         contentStream.endText();
         yPosition -= leading;

         contentStream.beginText();
         contentStream.newLineAtOffset(margin, yPosition);
         contentStream.showText("Khách hàng: " + (phieu.getKhachHang() != null ? phieu.getKhachHang().getHoTen() : "N/A"));
         contentStream.newLineAtOffset(300, 0);
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
         contentStream.newLineAtOffset(300, 0);
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


    // --- Hàm vẽ bảng (Helper method - Giữ nguyên logic vẽ) ---
    private void drawTable(PDPageContentStream contentStream, PDType0Font font, float y, float margin, PhieuSuaChua phieu, NumberFormat currencyFormatter) throws IOException {
        List<ChiTietSuaChua> chiTietList = phieu.getChiTietSuaChuaList();
        if (chiTietList == null) {
            chiTietList = List.of(); // Khởi tạo nếu null
            logger.warn("Danh sách chi tiết sửa chữa cho phiếu #{} là null.", phieu.getMaPhieu());
        }

        final int rows = chiTietList.size() + 1;
        final int cols = 4;
        final float rowHeight = 20f;
        final float tableWidth = 500f;
        final float[] colWidths = {200f, 80f, 110f, 110f};
         if (colWidths.length != cols) {
             throw new IllegalArgumentException("Số lượng chiều rộng cột không khớp số cột");
         }
         float sumWidth = 0;
         for(float w : colWidths) sumWidth += w;
         if(Math.abs(sumWidth - tableWidth) > 0.1) {
            logger.warn("Tổng chiều rộng các cột ({}) không bằng chiều rộng bảng ({})", sumWidth, tableWidth);
         }


        final float cellMargin = 5f;

        // --- Vẽ đường kẻ ---
        float nextY = y;
        for (int i = 0; i <= rows; i++) {
            contentStream.moveTo(margin, nextY);
            contentStream.lineTo(margin + tableWidth, nextY);
            contentStream.stroke();
            nextY -= rowHeight;
        }
        float nextX = margin;
        for (int i = 0; i <= cols; i++) {
            contentStream.moveTo(nextX, y);
            contentStream.lineTo(nextX, y - rows * rowHeight);
            contentStream.stroke();
            if (i < cols) {
                 nextX += colWidths[i];
            }
        }

        // --- Header của bảng ---
        float textX = margin + cellMargin;
        float textY = y - 15;
        String[] headers = {"Tên mục", "Số lượng", "Đơn giá", "Thành tiền"};
        contentStream.setFont(font, 10);
         for (int i = 0; i < headers.length; i++) {
             contentStream.beginText();
             float currentX = textX;
             float headerWidth = font.getStringWidth(headers[i]) / 1000 * 10;
             if (i == 1) { // Cột Số lượng (căn giữa)
                 currentX = textX + (colWidths[i] / 2) - (headerWidth / 2);
             } else if (i > 1) { // Cột Đơn giá, Thành tiền (căn phải)
                  currentX = textX + colWidths[i] - headerWidth - cellMargin*2;
             }
             contentStream.newLineAtOffset(currentX, textY);
             contentStream.showText(headers[i]);
             contentStream.endText();
             textX += colWidths[i];
         }
        textY -= rowHeight;

        // --- Dữ liệu chi tiết ---
        for (ChiTietSuaChua ct : chiTietList) {
             textX = margin + cellMargin;

             // Cột Tên mục (căn trái)
             contentStream.beginText();
             contentStream.newLineAtOffset(textX, textY);
             String tenLinhKien = ct.getLinhKien() != null ? ct.getLinhKien().getTenLinhKien() : "N/A";
             float maxWidthName = colWidths[0] - 2 * cellMargin;
             float nameWidth = font.getStringWidth(tenLinhKien) / 1000 * 10;
             if (nameWidth > maxWidthName) {
                 int charsToKeep = (int) (tenLinhKien.length() * (maxWidthName / nameWidth)) - 3;
                 charsToKeep = Math.max(0, charsToKeep);
                 tenLinhKien = tenLinhKien.substring(0, charsToKeep) + "...";
             }
             contentStream.showText(tenLinhKien);
             contentStream.endText();
             textX += colWidths[0];

             // Cột Số lượng (căn giữa)
             String soLuongStr = String.valueOf(ct.getSoLuong() != null ? ct.getSoLuong() : 0);
             float slWidth = font.getStringWidth(soLuongStr) / 1000 * 10;
             contentStream.beginText();
             contentStream.newLineAtOffset(textX + (colWidths[1] / 2) - (slWidth / 2), textY);
             contentStream.showText(soLuongStr);
             contentStream.endText();
             textX += colWidths[1];

             // Cột Đơn giá (căn phải)
             String donGiaStr = currencyFormatter.format(ct.getDonGia() != null ? ct.getDonGia() : java.math.BigDecimal.ZERO);
             float dgWidth = font.getStringWidth(donGiaStr) / 1000 * 10;
             contentStream.beginText();
             contentStream.newLineAtOffset(textX + colWidths[2] - dgWidth - cellMargin*2 , textY);
             contentStream.showText(donGiaStr);
             contentStream.endText();
             textX += colWidths[2];

             // Cột Thành tiền (căn phải)
             String thanhTienStr = currencyFormatter.format(ct.getThanhTien() != null ? ct.getThanhTien() : java.math.BigDecimal.ZERO);
             float ttWidth = font.getStringWidth(thanhTienStr) / 1000 * 10;
             contentStream.beginText();
             contentStream.newLineAtOffset(textX + colWidths[3] - ttWidth - cellMargin*2, textY);
             contentStream.showText(thanhTienStr);
             contentStream.endText();

             textY -= rowHeight;
         }
    }
}