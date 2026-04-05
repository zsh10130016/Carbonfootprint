package com.zhangsihan.carbonfootprint.security;

import com.zhangsihan.carbonfootprint.common.BusinessException;
import com.zhangsihan.carbonfootprint.common.ErrorCode;
import com.zhangsihan.carbonfootprint.entity.User;
import com.zhangsihan.carbonfootprint.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsernameOrEmail(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        return new UserPrincipal(user.getId(), user.getUsername(), user.getPassword(), user.getRole());
    }
}
