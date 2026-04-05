package com.zhangsihan.carbonfootprint.service;

import com.zhangsihan.carbonfootprint.common.BusinessException;
import com.zhangsihan.carbonfootprint.common.ErrorCode;
import com.zhangsihan.carbonfootprint.dto.LoginRequest;
import com.zhangsihan.carbonfootprint.dto.RegisterRequest;
import com.zhangsihan.carbonfootprint.dto.ResetPasswordRequest;
import com.zhangsihan.carbonfootprint.entity.User;
import com.zhangsihan.carbonfootprint.mapper.UserMapper;
import com.zhangsihan.carbonfootprint.security.JwtTokenProvider;
import com.zhangsihan.carbonfootprint.security.UserPrincipal;
import com.zhangsihan.carbonfootprint.vo.AuthResponseVO;
import com.zhangsihan.carbonfootprint.vo.UserProfileVO;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponseVO register(RegisterRequest request) {
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被占用");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole("USER");
        user.setCarbonGoal(BigDecimal.valueOf(50));
        userMapper.insert(user);
        return buildAuthResponse(user);
    }

    public AuthResponseVO login(LoginRequest request) {
        User user = userMapper.findByUsernameOrEmail(request.getAccount());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        return buildAuthResponse(user);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userMapper.findByUsernameAndEmail(request.getUsername(), request.getEmail());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "没有找到匹配的账号信息");
        }
        userMapper.updatePassword(user.getId(), passwordEncoder.encode(request.getNewPassword()));
    }

    public UserProfileVO toProfile(User user) {
        return UserProfileVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .location(user.getLocation())
                .bio(user.getBio())
                .carbonGoal(user.getCarbonGoal())
                .role(user.getRole())
                .build();
    }

    private AuthResponseVO buildAuthResponse(User user) {
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), user.getPassword(), user.getRole());
        return AuthResponseVO.builder()
                .token(jwtTokenProvider.createToken(principal))
                .tokenType("Bearer")
                .user(toProfile(user))
                .build();
    }
}
