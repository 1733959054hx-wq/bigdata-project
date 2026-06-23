import request from './request'

// 登录
export const userLogin = (data) => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 注册
export const userRegister = (data) => {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

// ✨✨✨ 新增：修改用户信息 ✨✨✨
export const updateUserInfo = (data) => {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

// ✨✨✨ 新增：修改密码 ✨✨✨
export const changePassword = (data) => {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })

  
}