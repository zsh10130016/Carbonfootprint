package com.zhangsihan.carbonfootprint.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CarbonRecord {
    private Long id;
    private Long userId;
    private String activityType;
    private String subType;
    private BigDecimal amount;
    private String unit;
    private BigDecimal emissionFactorValue;
    private BigDecimal emissionKg;
    private Integer points;
    private String note;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
