package com.zhangsihan.carbonfootprint.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PointsLedger {
    private Long id;
    private Long userId;
    private Long sourceRecordId;
    private Integer points;
    private String reason;
    private LocalDateTime createdAt;
}
