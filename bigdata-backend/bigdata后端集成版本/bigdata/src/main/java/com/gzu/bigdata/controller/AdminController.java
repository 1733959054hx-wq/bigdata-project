package com.gzu.bigdata.controller;

import com.gzu.bigdata.common.Result;
import com.gzu.bigdata.entity.Announcement;
import com.gzu.bigdata.entity.User;
import com.gzu.bigdata.service.AdminService;
import com.gzu.bigdata.utils.JwtUtil; // 务必确认已经创建了 utils 包和 JwtUtil 类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 核心方法：从请求头获取用户ID
     * 自动解析 Token 里的 UserId
     */
    private Long getUserId(HttpServletRequest request) {
        // 1. 尝试从 Header 的 "token" 字段获取
        String token = request.getHeader("token");

        // 2. 兼容处理：有时候 Token 会放在 "Authorization" 字段里 (Bearer xxx)
        if (token == null || token.isEmpty()) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                token = auth.substring(7);
            }
        }

        // 3. 使用我们写的 JwtUtil 解析 Token
        // 注意：如果没有登录或 Token 过期，这里会返回 null
        Long userId = JwtUtil.getUserId(token);

        // 4. 如果解析失败，返回 0L。
        // Service 层检测到 ID 为 0 或不存在时，会提示“用户不存在”或“权限不足”，不会崩掉。
        return userId != null ? userId : 0L;
    }

    // 1. 获取用户列表
    @GetMapping("/users")
    public Result<List<User>> getUserList(HttpServletRequest request) {
        return adminService.getUserList(getUserId(request));
    }

    // 2. 删除用户
    @DeleteMapping("/user/{id}")
    public Result<String> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        return adminService.deleteUser(getUserId(request), id);
    }

    // 3. 角色任命 (仅超管)
    // 前端传参: { "id": 用户ID, "role": 1 }
    @PostMapping("/role")
    public Result<String> changeRole(@RequestBody User user, HttpServletRequest request) {
        return adminService.updateUserRole(getUserId(request), user.getId(), user.getRole());
    }

    // 4. 发布公告
    @PostMapping("/announcement")
    public Result<String> publish(@RequestBody Announcement announcement, HttpServletRequest request) {
        return adminService.publishAnnouncement(getUserId(request), announcement);
    }

    // 5. 查看公告
    @GetMapping("/announcements")
    public Result<List<Announcement>> getAnnouncements() {
        return adminService.getAnnouncementList();
    }

    // 6. 修改密码
    // 前端传参：{ "id": 目标用户ID, "password": "新密码" }
    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody User user, HttpServletRequest request) {
        return adminService.updatePassword(getUserId(request), user.getId(), user.getPassword());
    }

    // 7. 修改用户状态 (启用/禁用)
    // 前端传参：{ "id": 用户ID, " "status": 0/1 }
    @PutMapping("/user/status")
    public Result<String> toggleStatus(@RequestBody User user, HttpServletRequest request) {
        return adminService.updateUserStatus(getUserId(request), user.getId(), user.getStatus());
    }

}