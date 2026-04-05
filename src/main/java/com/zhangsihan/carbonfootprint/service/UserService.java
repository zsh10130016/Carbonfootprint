package com.zhangsihan.carbonfootprint.service;

import com.zhangsihan.carbonfootprint.common.BusinessException;
import com.zhangsihan.carbonfootprint.common.ErrorCode;
import com.zhangsihan.carbonfootprint.dto.ChangePasswordRequest;
import com.zhangsihan.carbonfootprint.dto.UpdateProfileRequest;
import com.zhangsihan.carbonfootprint.entity.User;
import com.zhangsihan.carbonfootprint.mapper.UserMapper;
import com.zhangsihan.carbonfootprint.security.SecurityUtils;
import com.zhangsihan.carbonfootprint.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public UserProfileVO getCurrentUserProfile() {
        return authService.toProfile(getCurrentUser());
    }

    @Transactional
    public UserProfileVO updateProfile(UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = getCurrentUser();
        User existingEmailUser = userMapper.findByEmail(request.getEmail());
        if (existingEmailUser != null && !existingEmailUser.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被其他账号使用");
        }
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setLocation(request.getLocation());
        user.setBio(request.getBio());
        user.setCarbonGoal(request.getCarbonGoal());
        userMapper.updateProfile(user);
        return authService.toProfile(userMapper.findById(userId));
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "旧密码不正确");
        }
        userMapper.updatePassword(user.getId(), passwordEncoder.encode(request.getNewPassword()));
    }

    public User getCurrentUser() {
        User user = userMapper.findById(SecurityUtils.getCurrentUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }
}
