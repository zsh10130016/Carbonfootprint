package com.zhangsihan.carbonfootprint.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingItemVO {
    private Integer rank;
    private Long userId;
    private String username;
    private String fullName;
    private Integer totalPoints;
    private String badge;
}
