package com.baohanh.trungtambaohanh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatsDto {
    private long moiTiepNhan;
    private long dangSuaChua;
    private long choLinhKien;
    private long daHoanThanh;
    private long treHen; // Sẽ được phát triển logic sau
}