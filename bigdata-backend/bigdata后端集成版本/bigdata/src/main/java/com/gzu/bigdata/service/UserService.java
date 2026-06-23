package com.gzu.bigdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gzu.bigdata.dto.LoginDTO;
import com.gzu.bigdata.dto.RegisterDTO;
import com.gzu.bigdata.entity.User;

public interface UserService extends IService<User> {

    // 基础登录注册
    User login(LoginDTO loginDTO);
    boolean register(RegisterDTO registerDTO);

    // ✨✨✨ 新增：修改用户信息 ✨✨✨
    void updateUserInfo(Long userId, User user);

    // ✨✨✨ 新增：修改密码 ✨✨✨
    void updatePassword(Long userId, String oldPassword, String newPassword);
}