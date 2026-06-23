package com.gzu.bigdata.controller;

import com.gzu.bigdata.common.Result;
import com.gzu.bigdata.dto.LoginDTO;
import com.gzu.bigdata.dto.RegisterDTO;
import com.gzu.bigdata.entity.User;
import com.gzu.bigdata.service.UserService;
import com.gzu.bigdata.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 辅助方法：获取当前用户ID
    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("token");
        return JwtUtil.getUserId(token);
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO loginDTO) {
        User user = userService.login(loginDTO);
        String token = JwtUtil.createToken(user.getId(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return Result.success(data);
    }

    // ✨✨✨ 新增：修改个人信息接口 ✨✨✨
    @PutMapping("/update")
    public Result<?> updateInfo(@RequestBody User user, HttpServletRequest request) {
        Long userId = getUserId(request);
        userService.updateUserInfo(userId, user);

        // 返回更新后的用户信息，方便前端更新状态
        User updatedUser = userService.getById(userId);
        updatedUser.setPassword(null); // 擦除密码

        Map<String, Object> data = new HashMap<>();
        data.put("userInfo", updatedUser);
        return Result.success(data);
    }

    // ✨✨✨ 新增：修改密码接口 ✨✨✨
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        userService.updatePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功");
    }
}