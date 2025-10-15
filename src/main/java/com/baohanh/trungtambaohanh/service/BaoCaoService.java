package com.baohanh.trungtambaohanh.service;

import com.baohanh.trungtambaohanh.dto.ChartDataDto;
import com.baohanh.trungtambaohanh.dto.DoanhThuKpiDto;
import com.baohanh.trungtambaohanh.entity.PhieuSuaChua;
import java.time.OffsetDateTime;
import java.util.List;

public interface BaoCaoService {
	List<PhieuSuaChua> getPhieuSuaChuaHoanThanh(OffsetDateTime startDate, OffsetDateTime endDate, Integer ktvId);
    DoanhThuKpiDto tinhToanKpis(List<PhieuSuaChua> phieuList, OffsetDateTime startDate, OffsetDateTime endDate);
    ChartDataDto getDoanhThuTheoNgayChart(OffsetDateTime startDate, OffsetDateTime endDate, Integer ktvId);
    ChartDataDto getDoanhThuTheoLoaiThietBiChart(OffsetDateTime startDate, OffsetDateTime endDate, Integer ktvId);
}