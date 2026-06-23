package com.gzu.bigdata.service;

import com.gzu.bigdata.common.Result;
import com.gzu.bigdata.entity.Announcement;
import com.gzu.bigdata.entity.User;

import java.util.List;

public interface AdminService {
    // 获取用户列表（带权限过滤）
    Result<List<User>> getUserList(Long currentUserId);

    // 删除用户（带权限校验）
    Result<String> deleteUser(Long currentUserId, Long targetUserId);

    // 修改用户角色（仅超管）
    Result<String> updateUserRole(Long currentUserId, Long targetUserId, Integer newRole);

    // ✨✨✨ 新增：修改用户状态 (启用/禁用) ✨✨✨
    Result<String> updateUserStatus(Long currentUserId, Long targetUserId, Integer newStatus);

    // 发布公告
    Result<String> publishAnnouncement(Long currentUserId, Announcement announcement);

    // 获取所有公告
    Result<List<Announcement>> getAnnouncementList();

    // 修改密码（管理员功能）
    Result<String> updatePassword(Long currentUserId, Long targetUserId, String newPassword);
}