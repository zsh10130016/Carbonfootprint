package com.zhangsihan.carbonfootprint.controller;

import com.zhangsihan.carbonfootprint.common.ApiResponse;
import com.zhangsihan.carbonfootprint.common.BusinessException;
import com.zhangsihan.carbonfootprint.common.ErrorCode;
import com.zhangsihan.carbonfootprint.dto.LoginRequest;
import com.zhangsihan.carbonfootprint.dto.RegisterRequest;
import com.zhangsihan.carbonfootprint.dto.ResetPasswordRequest;
import com.zhangsihan.carbonfootprint.dto.SendEmailCodeRequest;
import com.zhangsihan.carbonfootprint.enums.EmailCodePurpose;
import com.zhangsihan.carbonfootprint.service.AuthService;
import com.zhangsihan.carbonfootprint.service.EmailVerificationService;
import com.zhangsihan.carbonfootprint.vo.AuthResponseVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/email-code")
    public ApiResponse<Void> sendEmailCode(@Valid @RequestBody SendEmailCodeRequest request) {
        EmailCodePurpose purpose;
        try {
            purpose = EmailCodePurpose.from(request.getPurpose());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, exception.getMessage());
        }
        emailVerificationService.sendCode(request.getEmail(), purpose);
        return ApiResponse.success(purpose.getDisplayName() + "验证码已发送", null);
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponseVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("注册成功", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponseVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("密码重置成功", null);
    }
}
