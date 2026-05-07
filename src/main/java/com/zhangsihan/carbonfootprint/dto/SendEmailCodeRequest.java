package com.zhangsihan.carbonfootprint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendEmailCodeRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * REGISTER: 注册验证码；RESET_PASSWORD: 重置密码验证码。
     */
    private String purpose = "REGISTER";
}
