import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userLogin } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  // 防止 JSON 解析报错
  let parsedUser = null
  try {
    parsedUser = JSON.parse(localStorage.getItem('userInfo') || 'null')
  } catch (e) {
    parsedUser = null
  }
  const userInfo = ref(parsedUser)

  const isLoggedIn = computed(() => !!token.value)
  
  // ✨✨✨ 核心修改：权限判断逻辑加入“后门” ✨✨✨
  // 只要用户名是 admin 或 superadmin，或者是 role=2，都算超级管理员
  const isSuperAdmin = computed(() => {
    const role = Number(userInfo.value?.role)
    const username = userInfo.value?.username
    return role === 2 || username === 'admin' || username === 'superadmin'
  })
  
  // 同理，admin 账号也自然拥有普通管理员权限
  const isAdmin = computed(() => {
    const role = Number(userInfo.value?.role)
    const username = userInfo.value?.username
    return role === 1 || role === 2 || username === 'admin' || username === 'superadmin'
  })

  const getUserAvatar = computed(() => {
    return userInfo.value?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec6438fd2643bcef8fd2c3jpeg.jpeg'
  })

  const login = async (loginForm) => {
    try {
      const res = await userLogin(loginForm)
      if (res.code === 200) {
        const userObj = res.data.user
        const tokenStr = res.data.token
        
        token.value = tokenStr
        userInfo.value = userObj
        
        localStorage.setItem('token', tokenStr)
        localStorage.setItem('userInfo', JSON.stringify(userObj))
        return true
      }
    } catch (error) {
      throw error
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    isSuperAdmin,
    getUserAvatar,
    login,
    logout
  }
})