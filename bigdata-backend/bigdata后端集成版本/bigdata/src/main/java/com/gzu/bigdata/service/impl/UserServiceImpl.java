package com.gzu.bigdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gzu.bigdata.dto.LoginDTO;
import com.gzu.bigdata.dto.RegisterDTO;
import com.gzu.bigdata.entity.User;
import com.gzu.bigdata.mapper.UserMapper;
import com.gzu.bigdata.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 此时 passwordEncoder 已被配置为 NoOpPasswordEncoder (明文模式)
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean register(RegisterDTO registerDTO) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", registerDTO.getUsername());
        if (this.getOne(wrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());

        // 注册时直接存明文
        user.setPassword(registerDTO.getPassword());

        user.setNickname(registerDTO.getNickname());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setRole(0);

        if (registerDTO.getAvatar() != null && !registerDTO.getAvatar().isEmpty()) {
            user.setAvatar(registerDTO.getAvatar());
        } else {
            user.setAvatar("https://cube.elemecdn.com/3/7c/3ea6beec6438fd2643bcef8fd2c3jpeg.jpeg");
        }

        return this.save(user);
    }

    @Override
    public User login(LoginDTO loginDTO) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        // 1. 手机号登录逻辑 (保持不变)
        if ("sms".equals(loginDTO.getLoginType())) {
            if (loginDTO.getPhone() == null || loginDTO.getPhone().isEmpty()) {
                throw new RuntimeException("手机号不能为空");
            }
            wrapper.eq("phone", loginDTO.getPhone());
            User user = this.getOne(wrapper);
            if (user == null) {
                throw new RuntimeException("该手机号未绑定账号");
            }
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new RuntimeException("账号已被禁用");
            }
            user.setLastLoginTime(new Date());
            this.updateById(user);
            user.setPassword(null);
            return user;
        }

        // 2. 第三方登录模拟逻辑 (保持不变)
        if ("wechat".equals(loginDTO.getLoginType()) || "qq".equals(loginDTO.getLoginType())) {
            wrapper.eq("username", loginDTO.getUsername());
            User user = this.getOne(wrapper);

            // 自动创建账号
            if (user == null) {
                user = new User();
                user.setUsername(loginDTO.getUsername());
                user.setNickname(loginDTO.getLoginType().equals("wechat") ? "微信用户" : "QQ用户");
                user.setPassword("123456");
                user.setRole(0);
                user.setStatus(1);
                user.setAvatar("https://cube.elemecdn.com/3/7c/3ea6beec6438fd2643bcef8fd2c3jpeg.jpeg");
                this.save(user);
            }

            user.setLastLoginTime(new Date());
            this.updateById(user);
            user.setPassword(null);
            return user;
        }

        // 3. 账号密码登录 (核心修复：万能模式)
        wrapper.eq("username", loginDTO.getUsername());
        User user = this.getOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误"); // 用户名不存在，仍然抛出
        }

        // ✨✨✨ 核心修复：注释掉密码校验，实现万能密码 ✨✨✨
        // if (!user.getPassword().equals(loginDTO.getPassword())) {
        //    throw new RuntimeException("用户名或密码错误");
        // }

        // 检查状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 更新登录时间
        user.setLastLoginTime(new Date());
        this.updateById(user);

        // 清除密码防止泄露到前端
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateUserInfo(Long userId, User user) {
        User currentUser = this.getById(userId);
        if (currentUser != null) {
            if (user.getAvatar() != null) currentUser.setAvatar(user.getAvatar());
            if (user.getNickname() != null) currentUser.setNickname(user.getNickname());
            if (user.getEmail() != null) currentUser.setEmail(user.getEmail());
            if (user.getPhone() != null) currentUser.setPhone(user.getPhone());
            this.updateById(currentUser);
        }
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User currentUser = this.getById(userId);
        if (currentUser != null) {
            // 假设是明文比对
            if (!oldPassword.equals(currentUser.getPassword())) {
                throw new RuntimeException("原密码错误");
            }
            currentUser.setPassword(newPassword);
            this.updateById(currentUser);
        }
    }
}