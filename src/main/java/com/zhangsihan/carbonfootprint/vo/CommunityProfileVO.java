package com.zhangsihan.carbonfootprint.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommunityProfileVO {
    private Integer rank;
    private Integer totalPoints;
    private Integer totalRecords;
    private BigDecimal totalEmission;
}
