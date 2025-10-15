package com.baohanh.trungtambaohanh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private BigDecimal totalRevenue;
    private long totalTickets;
    private long completedTickets;
    private String averageRepairTime; // Sẽ hiển thị dạng "X ngày Y giờ"
}