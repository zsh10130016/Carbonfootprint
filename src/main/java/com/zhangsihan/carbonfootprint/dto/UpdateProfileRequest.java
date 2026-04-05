package com.zhangsihan.carbonfootprint.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "姓名不能为空")
    private String fullName;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    private String location;

    private String bio;

    @DecimalMin(value = "0.0", inclusive = true, message = "目标值不能小于 0")
    private BigDecimal carbonGoal;
}
