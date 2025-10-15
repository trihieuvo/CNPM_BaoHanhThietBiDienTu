package com.baohanh.trungtambaohanh.service;

import com.baohanh.trungtambaohanh.dto.ChartDataDto;
import com.baohanh.trungtambaohanh.dto.DoanhThuKpiDto;
import com.baohanh.trungtambaohanh.dto.LinhKienSuDungDto;
import com.baohanh.trungtambaohanh.dto.ThongKeSuaChuaKpiDto;
import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import com.baohanh.trungtambaohanh.repository.ChiTietSuaChuaRepository;
import com.baohanh.trungtambaohanh.repository.PhieuSuaChuaRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BaoCaoServiceImpl implements BaoCaoService {

    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final ChiTietSuaChuaRepository chiTietSuaChuaRepository;
    
    public BaoCaoServiceImpl(PhieuSuaChuaRepository phieuSuaChuaRepository, ChiTietSuaChuaRepository chiTietSuaChuaRepository) {
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.chiTietSuaChuaRepository = chiTietSuaChuaRepository;
    }

    @Override
    public List<PhieuSuaChua> getPhieuSuaChuaHoanThanh(OffsetDateTime startDate, OffsetDateTime endDate, Integer ktvId) {
        return phieuSuaChuaRepository.findCompletedTicketsInDateRange(startDate, endDate, ktvId);
    }

    @Override
    public DoanhThuKpiDto tinhToanKpis(List<PhieuSuaChua> phieuList, OffsetDateTime startDate, OffsetDateTime endDate) {
        DoanhThuKpiDto kpis = new DoanhThuKpiDto();
        if (phieuList == null || phieuList.isEmpty()) {
            return kpis;
        }

        BigDecimal tongDoanhThu = phieuList.stream()
                .map(PhieuSuaChua::getTongChiPhi)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long tongSoPhieu = phieuList.size();
        
        BigDecimal doanhThuTrungBinh = (tongSoPhieu > 0)
                ? tongDoanhThu.divide(BigDecimal.valueOf(tongSoPhieu), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        kpis.setTongDoanhThu(tongDoanhThu);
        kpis.setTongSoPhieuHoanThanh(tongSoPhieu);
        kpis.setDoanhThuTrungBinh(doanhThuTrungBinh);
        
        List<Object[]> topPart = chiTietSuaChuaRepository.findTopRevenuePartInDateRange(startDate, endDate, PageRequest.of(0, 1));
        if (!topPart.isEmpty()) {
            kpis.setLinhKienDoanhThuCaoNhat((String) topPart.get(0)[0]);
        }
        return kpis;
    }

    @Override
    public ChartDataDto getDoanhThuTheoNgayChart(OffsetDateTime startDate, OffsetDateTime endDate, Integer ktvId) {
        List<Object[]> results = phieuSuaChuaRepository.getRevenueByDay(startDate, endDate, ktvId); // Truyền ktvId
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (Object[] result : results) {
            OffsetDateTime date = (OffsetDateTime) result[0];
            BigDecimal revenue = (BigDecimal) result[1];
            labels.add(date.format(formatter));
            data.add(revenue);
        }
        return new ChartDataDto(labels, data);
    }

    @Override
    public ChartDataDto getDoanhThuTheoLoaiThietBiChart(OffsetDateTime startDate, OffsetDateTime endDate, Integer ktvId) {
        List<Object[]> results = phieuSuaChuaRepository.getRevenueByDeviceType(startDate, endDate, ktvId); // Truyền ktvId
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        for (Object[] result : results) {
            labels.add((String) result[0]);
            data.add((BigDecimal) result[1]);
        }
        return new ChartDataDto(labels, data);
    }
    
    
    @Override
    public ThongKeSuaChuaKpiDto tinhToanThongKeSuaChuaKpis(OffsetDateTime startDate, OffsetDateTime endDate, Integer ktvId, Integer loaiThietBiId) {
        ThongKeSuaChuaKpiDto kpis = new ThongKeSuaChuaKpiDto();
        List<Object[]> statusCounts = phieuSuaChuaRepository.countTicketsByStatus(startDate, endDate, ktvId, loaiThietBiId);
        
        long tiepNhan = 0;
        long hoanThanh = 0;
        for(Object[] status : statusCounts) {
            String trangThai = (String) status[0];
            long count = (long) status[1];
            tiepNhan += count; // Tổng tất cả các phiếu trong kỳ
            if ("Đã trả khách".equals(trangThai)) {
                hoanThanh += count;
            }
        }
        kpis.setTongPhieuTiepNhan(tiepNhan);
        kpis.setTongPhieuHoanThanh(hoanThanh);

        // Tính thời gian sửa trung bình
        List<PhieuSuaChua> completedTickets = phieuSuaChuaRepository.findTicketsForAverageTime(startDate, endDate, ktvId, loaiThietBiId);
        if (!completedTickets.isEmpty()) {
            long totalDurationSeconds = 0;
            for (PhieuSuaChua ticket : completedTickets) {
                Duration duration = Duration.between(ticket.getNgayTiepNhan(), ticket.getNgayHoanThanh());
                totalDurationSeconds += duration.getSeconds();
            }
            long avgSeconds = totalDurationSeconds / completedTickets.size();
            long days = TimeUnit.SECONDS.toDays(avgSeconds);
            long hours = TimeUnit.SECONDS.toHours(avgSeconds) % 24;
            kpis.setThoiGianSuaTrungBinh(String.format("%d ngày %d giờ", days, hours));
        }

        return kpis;
    }

    @Override
    public ChartDataDto getPhieuTheoTrangThaiChart(OffsetDateTime startDate, OffsetDateTime endDate, Integer ktvId, Integer loaiThietBiId) {
        List<Object[]> results = phieuSuaChuaRepository.countTicketsByStatus(startDate, endDate, ktvId, loaiThietBiId);
        List<String> labels = results.stream().map(r -> (String) r[0]).collect(Collectors.toList());
        List<BigDecimal> data = results.stream().map(r -> new BigDecimal((long) r[1])).collect(Collectors.toList());
        return new ChartDataDto(labels, data);
    }

    @Override
    public ChartDataDto getPhieuTheoKtvChart(OffsetDateTime startDate, OffsetDateTime endDate, Integer loaiThietBiId) {
        List<Object[]> results = phieuSuaChuaRepository.countCompletedTicketsByTechnician(startDate, endDate, loaiThietBiId);
        List<String> labels = results.stream().map(r -> (String) r[0]).collect(Collectors.toList());
        List<BigDecimal> data = results.stream().map(r -> new BigDecimal((long) r[1])).collect(Collectors.toList());
        return new ChartDataDto(labels, data);
    }

    @Override
    public List<LinhKienSuDungDto> getThongKeSuDungLinhKien(OffsetDateTime startDate, OffsetDateTime endDate, Integer ktvId, Integer loaiThietBiId) {
        return chiTietSuaChuaRepository.getLinhKienUsageStats(startDate, endDate, ktvId, loaiThietBiId);
    }
}