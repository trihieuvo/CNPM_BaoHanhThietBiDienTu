package com.baohanh.trungtambaohanh.service;

import com.baohanh.trungtambaohanh.dto.DashboardStatsDto;
import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import com.baohanh.trungtambaohanh.repository.PhieuSuaChuaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final PhieuSuaChuaRepository phieuSuaChuaRepository;

    public DashboardServiceImpl(PhieuSuaChuaRepository phieuSuaChuaRepository) {
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
    }

    @Override
    public DashboardStatsDto getDashboardStats() {
        // Lấy dữ liệu trong tháng này
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();
        OffsetDateTime startDateTime = firstDayOfMonth.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endDateTime = today.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        // Sử dụng lại query đã có để lấy dữ liệu
        List<PhieuSuaChua> completedTicketsThisMonth = phieuSuaChuaRepository.findCompletedTicketsInDateRange(startDateTime, endDateTime, null);

        BigDecimal totalRevenue = completedTicketsThisMonth.stream()
                .map(PhieuSuaChua::getTongChiPhi)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTickets = phieuSuaChuaRepository.count(); // Tổng số phiếu từ trước đến nay
        long completedTickets = completedTicketsThisMonth.size(); // Số phiếu hoàn thành trong tháng
        String averageRepairTime = calculateAverageRepairTime();

        return new DashboardStatsDto(totalRevenue, totalTickets, completedTickets, averageRepairTime);
    }

    private String calculateAverageRepairTime() {
        // Giữ nguyên logic cũ tính trên toàn bộ dữ liệu
        List<PhieuSuaChua> allCompletedTickets = phieuSuaChuaRepository.findAllCompletedWithTimestamps();
        if (allCompletedTickets.isEmpty()) {
            return "N/A";
        }

        long totalDurationSeconds = 0;
        for (PhieuSuaChua ticket : allCompletedTickets) {
            if (ticket.getNgayTiepNhan() != null && ticket.getNgayHoanThanh() != null) {
                Duration duration = Duration.between(ticket.getNgayTiepNhan(), ticket.getNgayHoanThanh());
                totalDurationSeconds += duration.getSeconds();
            }
        }
        
        if (allCompletedTickets.size() == 0 || totalDurationSeconds == 0) return "N/A";

        long avgSeconds = totalDurationSeconds / allCompletedTickets.size();
        long days = TimeUnit.SECONDS.toDays(avgSeconds);
        long hours = TimeUnit.SECONDS.toHours(avgSeconds) % 24;
        
        return String.format("~ %d ngày %d giờ", days, hours);
    }
}