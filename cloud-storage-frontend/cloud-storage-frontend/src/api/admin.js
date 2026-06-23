import request from './request'

// 1. 获取用户列表
export const getUserList = () => {
  return request({
    url: '/admin/users',
    method: 'get'
  })
}

// 2. 删除用户
export const deleteUser = (userId) => {
  return request({
    url: `/admin/user/${userId}`,
    method: 'delete'
  })
}

// 3. 修改角色 (仅超管可用)
export const changeUserRole = (data) => {
  return request({
    url: '/admin/role',
    method: 'post',
    data
  })
}

// 4. 修改密码 (管理员重置他人密码)
export const adminUpdatePassword = (data) => {
  return request({
    url: '/admin/password',
    method: 'put',
    data
  })
}

// 5. 发布公告
export const publishAnnouncement = (data) => {
  return request({
    url: '/admin/announcement',
    method: 'post',
    data
  })
}


// 7. 修改用户状态 (启用/禁用)
export const updateUserStatus = (userId, status) => {
  return request({
    url: '/admin/user/status',
    method: 'put',
    data: { id: userId, status: status }
  })
}