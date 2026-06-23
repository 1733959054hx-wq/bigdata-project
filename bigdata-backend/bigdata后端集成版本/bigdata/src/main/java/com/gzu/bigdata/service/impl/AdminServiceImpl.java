package com.gzu.bigdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gzu.bigdata.common.Result;
import com.gzu.bigdata.entity.Announcement;
import com.gzu.bigdata.entity.User;
import com.gzu.bigdata.mapper.AnnouncementMapper;
import com.gzu.bigdata.mapper.UserMapper;
import com.gzu.bigdata.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 辅助方法
    private User getOperator(Long uid) {
        if (uid == null) return null;
        return userMapper.selectById(uid);
    }
    // 仅展示新增和修改部分
// 请在 AdminServiceImpl.java 中添加以下方法：

    @Override
    public Result<String> updateUserStatus(Long currentUserId, Long targetUserId, Integer newStatus) {
        User op = getOperator(currentUserId);
        User target = userMapper.selectById(targetUserId);

        if (op == null || target == null) return Result.fail("用户不存在");
        // 普通管理员不能禁用/启用比自己权限高的用户
        if (op.getRole() == 0) return Result.fail("权限不足");
        if (op.getRole() == 1 && target.getRole() != 0) return Result.fail("权限不足");
        if (op.getRole() == 2 && op.getId().equals(target.getId())) return Result.fail("不能操作自己");

        // 仅更新状态字段
        User update = new User();
        update.setId(targetUserId);
        update.setStatus(newStatus);
        userMapper.updateById(update);

        return Result.success(newStatus == 1 ? "启用成功" : "禁用成功");
    }

    @Override
    public Result<List<User>> getUserList(Long currentUserId) {
        User op = getOperator(currentUserId);
        if (op == null || op.getRole() == null || op.getRole() == 0) {
            return Result.fail("无权访问");
        }

        QueryWrapper<User> query = new QueryWrapper<>();
        if (op.getRole() == 1) {
            query.eq("role", 0);
        }

        List<User> list = userMapper.selectList(query);
        list.forEach(u -> u.setPassword(null));
        return Result.success(list);
    }

    @Override
    public Result<String> deleteUser(Long currentUserId, Long targetUserId) {
        User op = getOperator(currentUserId);
        User target = userMapper.selectById(targetUserId);

        if (op == null || target == null) return Result.fail("用户不存在");
        if (op.getRole() == 0) return Result.fail("权限不足");
        if (op.getRole() == 1 && target.getRole() != 0) return Result.fail("权限不足");
        if (op.getRole() == 2 && op.getId().equals(target.getId())) return Result.fail("不能删除自己");

        userMapper.deleteById(targetUserId);
        return Result.success("删除成功");
    }

    @Override
    public Result<String> updateUserRole(Long currentUserId, Long targetUserId, Integer newRole) {
        User op = getOperator(currentUserId);
        if (op == null || op.getRole() != 2) return Result.fail("仅超管可操作");

        User target = userMapper.selectById(targetUserId);
        if (target == null) return Result.fail("用户不存在");

        target.setRole(newRole);
        userMapper.updateById(target);
        return Result.success("角色修改成功");
    }

    // ✨✨✨ 公告发布核心修复：手动设置时间和发布者ID ✨✨✨
    @Override
    public Result<String> publishAnnouncement(Long currentUserId, Announcement announcement) {
        User op = getOperator(currentUserId);
        if (op == null || op.getRole() == 0) return Result.fail("权限不足");

        announcement.setCreatorId(currentUserId);
        announcement.setCreateTime(new Date());
        announcement.setUpdateTime(new Date());

        announcementMapper.insert(announcement);
        return Result.success("公告发布成功");
    }

    @Override
    public Result<List<Announcement>> getAnnouncementList() {
        QueryWrapper<Announcement> query = new QueryWrapper<>();
        query.orderByDesc("create_time");
        return Result.success(announcementMapper.selectList(query));
    }

    @Override
    public Result<String> updatePassword(Long currentUserId, Long targetUserId, String newPassword) {
        User op = getOperator(currentUserId);
        User target = userMapper.selectById(targetUserId);

        if (op == null || target == null) return Result.fail("用户不存在");

        if (!op.getId().equals(target.getId())) {
            if (op.getRole() == 0) return Result.fail("权限不足");
            if (op.getRole() == 1 && target.getRole() != 0) return Result.fail("权限不足");
            if (op.getRole() == 2 && target.getRole() == 2) return Result.fail("权限不足");
        }

        target.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(target);
        return Result.success("密码修改成功");
    }
}