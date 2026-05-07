package com.zhangsihan.carbonfootprint.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SubtypeEmissionStatVO {
    private String activityType;
    private String subType;
    private BigDecimal emissionKg;
}
