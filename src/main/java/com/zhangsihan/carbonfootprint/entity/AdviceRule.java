package com.zhangsihan.carbonfootprint.entity;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AdviceRule {
    private Long id;
    private String activityType;
    private BigDecimal thresholdKg;
    private Integer periodDays;
    private String title;
    private String description;
    private String suggestion;
}
