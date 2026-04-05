package com.zhangsihan.carbonfootprint.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CarbonRecordCreateRequest {
    @NotBlank(message = "活动类型不能为空")
    private String activityType;

    @NotBlank(message = "子类型不能为空")
    private String subType;

    @NotNull(message = "活动数值不能为空")
    @DecimalMin(value = "0.01", message = "活动数值必须大于 0")
    private BigDecimal amount;

    @NotBlank(message = "单位不能为空")
    private String unit;

    private String note;

    @NotNull(message = "记录时间不能为空")
    private LocalDateTime occurredAt;
}
