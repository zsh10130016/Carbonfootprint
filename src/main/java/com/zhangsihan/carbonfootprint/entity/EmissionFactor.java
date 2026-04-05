package com.zhangsihan.carbonfootprint.entity;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class EmissionFactor {
    private Long id;
    private String activityType;
    private String subType;
    private String unit;
    private BigDecimal factorValue;
    private String factorName;
    private String description;
}
