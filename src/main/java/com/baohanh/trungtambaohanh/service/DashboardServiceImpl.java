package com.baohanh.trungtambaohanh.service;

import com.baohanh.trungtambaohanh.dto.DashboardStatsDto;
import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import com.baohanh.trungtambaohanh.repository.PhieuSuaChuaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
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
        BigDecimal totalRevenue = Optional.ofNullable(phieuSuaChuaRepository.findTotalRevenue()).orElse(BigDecimal.ZERO);
        long totalTickets = phieuSuaChuaRepository.count();
        long completedTickets = phieuSuaChuaRepository.countByTrangThai("Đã trả khách");
        String averageRepairTime = calculateAverageRepairTime();

        return new DashboardStatsDto(totalRevenue, totalTickets, completedTickets, averageRepairTime);
    }

    private String calculateAverageRepairTime() {
        List<PhieuSuaChua> completedTickets = phieuSuaChuaRepository.findAllCompletedWithTimestamps();
        if (completedTickets.isEmpty()) {
            return "N/A";
        }

        long totalDurationSeconds = 0;
        for (PhieuSuaChua ticket : completedTickets) {
            Duration duration = Duration.between(ticket.getNgayTiepNhan(), ticket.getNgayHoanThanh());
            totalDurationSeconds += duration.getSeconds();
        }

        long avgSeconds = totalDurationSeconds / completedTickets.size();

        // Chuyển đổi giây trung bình thành định dạng ngày, giờ, phút
        long days = TimeUnit.SECONDS.toDays(avgSeconds);
        long hours = TimeUnit.SECONDS.toHours(avgSeconds) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(avgSeconds) % 60;

        return String.format("%d ngày %d giờ %d phút", days, hours, minutes);
    }
}