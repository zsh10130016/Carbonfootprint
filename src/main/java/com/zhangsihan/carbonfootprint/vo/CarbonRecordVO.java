package com.zhangsihan.carbonfootprint.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarbonRecordVO {
    private Long id;
    private String activityType;
    private String activityLabel;
    private String subType;
    private BigDecimal amount;
    private String unit;
    private BigDecimal emissionFactorValue;
    private BigDecimal emissionKg;
    private Integer points;
    private String note;
    private LocalDateTime occurredAt;
}
