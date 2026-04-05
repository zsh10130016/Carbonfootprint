package com.zhangsihan.carbonfootprint.mapper;

import com.zhangsihan.carbonfootprint.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findById(Long id);

    User findByUsername(String username);

    User findByEmail(String email);

    User findByUsernameOrEmail(String account);

    User findByUsernameAndEmail(@Param("username") String username, @Param("email") String email);

    int insert(User user);

    int updateProfile(User user);

    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
