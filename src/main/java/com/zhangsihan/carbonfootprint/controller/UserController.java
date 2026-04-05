package com.zhangsihan.carbonfootprint.controller;

import com.zhangsihan.carbonfootprint.common.ApiResponse;
import com.zhangsihan.carbonfootprint.dto.ChangePasswordRequest;
import com.zhangsihan.carbonfootprint.dto.UpdateProfileRequest;
import com.zhangsihan.carbonfootprint.service.UserService;
import com.zhangsihan.carbonfootprint.vo.UserProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserProfileVO> getProfile() {
        return ApiResponse.success(userService.getCurrentUserProfile());
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("个人信息已更新", userService.updateProfile(request));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.success("密码修改成功", null);
    }
}
