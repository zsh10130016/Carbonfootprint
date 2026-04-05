package com.zhangsihan.carbonfootprint.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdviceVO {
    private String activityType;
    private String title;
    private String description;
    private String suggestion;
    private BigDecimal actualEmission;
    private BigDecimal threshold;
    private Integer periodDays;
}
