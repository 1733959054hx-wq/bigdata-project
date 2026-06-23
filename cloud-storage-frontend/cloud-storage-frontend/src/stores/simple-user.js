// src/stores/simple-user.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useSimpleUserStore = defineStore('simpleUser', () => {
  const userInfo = ref(null)
  const token = ref('')
  
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin' || userInfo.value?.role === 'superadmin')
  const isSuperAdmin = computed(() => userInfo.value?.role === 'superadmin')
  const getUserAvatar = computed(() => userInfo.value?.avatar || '/default-avatar.png')
  
  // 从 localStorage 初始化
  const initFromStorage = () => {
    const savedToken = localStorage.getItem('token')
    const savedUserInfo = localStorage.getItem('userInfo')
    
    if (savedToken) token.value = savedToken
    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
  }
  
  const logout = () => {
    userInfo.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
  
  // 立即初始化
  initFromStorage()
  
  return {
    userInfo,
    token,
    isLoggedIn,
    isAdmin,
    isSuperAdmin,
    getUserAvatar,
    logout,
    initFromStorage
  }
})