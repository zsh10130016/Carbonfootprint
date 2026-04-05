package com.zhangsihan.carbonfootprint.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatVO {
    private String name;
    private BigDecimal emissionKg;
    private Integer points;
}
