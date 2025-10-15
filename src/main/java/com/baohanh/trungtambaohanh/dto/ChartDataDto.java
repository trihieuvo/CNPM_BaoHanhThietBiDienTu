package com.baohanh.trungtambaohanh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDto {
    private List<String> labels;
    private List<BigDecimal> data;
}