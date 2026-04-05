package com.zhangsihan.carbonfootprint.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryVO {
    private BigDecimal totalEmission;
    private BigDecimal weekEmission;
    private Integer totalPoints;
    private Integer totalRecords;
    private String topSource;
}
